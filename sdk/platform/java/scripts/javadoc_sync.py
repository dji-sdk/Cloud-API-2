#!/usr/bin/env python3
"""
从 message.h 提取 struct 字段注释，同步到手写 Java bean。
补三处：字段声明、getter、setter。
- 无注释 → 插入 C++ 注释
- 有注释且与 C++ 一致 → 跳过
- 有注释但与 C++ 不一致 → 替换为 C++ 注释（C++ 是 ground truth）

用法:
  python3 javadoc_sync.py --message-h <path> --bean-dir <path> --convert <path> [--dry-run]
"""
from __future__ import annotations

import argparse
import os
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional, Dict, List


@dataclass
class StructField:
    name: str
    cpp_type: str
    comment: str


@dataclass
class StructInfo:
    name: str
    fields: List[StructField] = field(default_factory=list)


@dataclass
class BeanMethod:
    """getter 或 setter"""
    kind: str          # "get" or "set"
    java_field: str    # 对应的 Java 字段名
    line_num: int      # 方法声明行号
    has_comment: bool  # 上方是否已有注释
    comment: str       # 上方已有注释内容（空字符串表示无注释）
    param_name: str = ""  # setter 的参数名


@dataclass
class BeanFieldDecl:
    """Java bean 字段声明"""
    java_name: str
    line_num: int
    has_comment: bool
    comment: str  # 已有注释内容


@dataclass
class BeanInfo:
    class_name: str
    file_path: str
    fields: List[BeanFieldDecl] = field(default_factory=list)
    methods: List[BeanMethod] = field(default_factory=list)


def parse_message_h(filepath: str) -> Dict[str, StructInfo]:
    """解析 message.h，提取 struct 字段注释。"""
    structs = {}
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    i = 0
    while i < len(lines):
        line = lines[i].strip()
        m = re.match(r'^(?:struct|class)\s+(\w+)\s*\{', line)
        if not m:
            i += 1
            continue

        struct_name = m.group(1)
        struct = StructInfo(name=struct_name)
        i += 1
        brace_depth = 1

        while i < len(lines) and brace_depth > 0:
            inner = lines[i].strip()
            if '{' in inner:
                brace_depth += inner.count('{')
            if '}' in inner:
                brace_depth -= inner.count('}')
                if brace_depth <= 0:
                    break

            if 'operator==' in inner or 'operator!=' in inner or 'AIGC_JSON_HELPER' in inner:
                i += 1
                continue

            field_pattern = re.compile(
                r'^(?:const\s+)?'
                r'(std::\S+|[A-Za-z_]\w*(?:\s*<[^>]+>)?|unsigned\s+\w+|long\s+long|int64_t|int32_t|int16_t|int8_t|double|float|bool|int|long|short|char|std::string)'
                r'\s+'
                r'(\w+)'
                r'\s*(?:=\s*[^;]+)?'
                r'\s*;'
                r'\s*(?:///?\s*(.*))?',
                re.IGNORECASE
            )
            fm = field_pattern.match(inner)
            if fm:
                cpp_type = fm.group(1)
                field_name = fm.group(2)
                comment = fm.group(3) or ""
                comment = comment.strip()
                if comment.startswith("< "):
                    comment = comment[2:]
                elif comment.startswith("<"):
                    comment = comment[1:].strip()

                if field_name.startswith('swig'):
                    i += 1
                    continue
                struct.fields.append(StructField(name=field_name, cpp_type=cpp_type, comment=comment))

            i += 1

        if struct.fields:
            structs[struct_name] = struct

    return structs


def snake_to_camel(name: str) -> str:
    parts = name.split('_')
    return parts[0] + ''.join(p.capitalize() for p in parts[1:])


def parse_convert_file(filepath: str) -> Dict[str, Dict[str, str]]:
    """解析 Convert 函数，提取 java_field → cpp_field 映射。"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    mappings = {}
    func_pattern = re.compile(
        r'public\s+static\s+'
        r'com\.dji\.flighthubsdk\.generated\.(\w+)\s+'
        r'toNative\w*\s*\('
        r'[^)]*'
        r'com\.dji\.flighthubsdk\.bean\.(\w+)\s+\w+\)',
        re.DOTALL
    )

    for func_match in func_pattern.finditer(content):
        struct_name = func_match.group(1)
        func_start = func_match.end()
        brace_depth = 0
        func_body_start = -1
        pos = func_start

        while pos < len(content):
            if content[pos] == '{':
                brace_depth += 1
                if func_body_start == -1:
                    func_body_start = pos + 1
            elif content[pos] == '}':
                brace_depth -= 1
                if brace_depth == 0:
                    break
            pos += 1

        if func_body_start == -1:
            continue

        func_body = content[func_body_start:pos]
        set_get_pattern = re.compile(
            r'nativeParams\.set(\w+)\s*\(\s*(?:toNative\w+\s*\(\s*)?params\.get(\w+)\s*\(',
        )

        field_map = {}
        for sm in set_get_pattern.finditer(func_body):
            cpp_field_snake = sm.group(1)
            java_field = sm.group(2)
            cpp_field = cpp_field_snake[0].lower() + cpp_field_snake[1:]
            field_map[java_field] = cpp_field

        if field_map:
            mappings[struct_name] = field_map

    return mappings


def has_comment_above(lines: List[str], line_idx: int) -> bool:
    """检查指定行上方是否紧邻 Javadoc 注释。"""
    j = line_idx - 1
    while j >= 0 and lines[j].strip() == '':
        j -= 1
    if j < 0:
        return False
    return lines[j].strip().endswith('*/')


def get_comment_block(lines: List[str], line_idx: int) -> Optional[tuple]:
    """返回 (comment_text, start_line, end_line) 或 None。
    
    查找 line_idx 上方紧邻的 Javadoc 注释块。
    start_line/end_line 是注释块在 lines 中的索引（含）。
    """
    j = line_idx - 1
    while j >= 0 and lines[j].strip() == '':
        j -= 1
    if j < 0 or not lines[j].strip().endswith('*/'):
        return None
    end = j
    while j >= 0 and '/**' not in lines[j]:
        j -= 1
    if j < 0:
        return None
    start = j
    raw = ''.join(lines[start:end+1]).strip()
    inner = re.sub(r'^/\*\*', '', raw)
    inner = re.sub(r'\*/$', '', inner)
    inner = re.sub(r'^\s*\*\s?', '', inner, flags=re.MULTILINE)
    return (inner.strip(), start, end)


def parse_bean(filepath: str) -> Optional[BeanInfo]:
    """解析手写 bean，提取字段声明和 getter/setter 方法。"""
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    class_name = Path(filepath).stem
    bean = BeanInfo(class_name=class_name, file_path=filepath)

    for i, line in enumerate(lines):
        stripped = line.strip()

        # 字段声明: private int page = 1; / private Long beginAt;
        field_pattern = re.compile(
            r'^private\s+(\S+)\s+(\w+)\s*(?:=[^;]+)?\s*;'
        )
        fm = field_pattern.match(stripped)
        if fm:
            java_name = fm.group(2)
            block = get_comment_block(lines, i)
            bean.fields.append(BeanFieldDecl(
                java_name=java_name,
                line_num=i,
                has_comment=block is not None,
                comment=block[0] if block else "",
            ))
            continue

        # getter: public int getPage() {  或  public String getName() {  或  public boolean isLogToConsole() {
        getter_pattern = re.compile(
            r'^public\s+(?:\S+)\s+(?:get|is)(\w+)\s*\(\s*\)\s*\{'
        )
        gm = getter_pattern.match(stripped)
        if gm:
            getter_suffix = gm.group(1)
            java_field = getter_suffix[0].lower() + getter_suffix[1:]
            if java_field.startswith('swig') or getter_suffix == 'CPtr':
                continue
            block = get_comment_block(lines, i)
            bean.methods.append(BeanMethod(
                kind="get", java_field=java_field,
                line_num=i, has_comment=block is not None,
                comment=block[0] if block else "",
            ))
            continue

        # setter: public void setPage(int page) {  /  public void setSn(List<String> sn) {
        setter_pattern = re.compile(
            r'^public\s+void\s+set(\w+)\s*\(\s*\S+\s+(\w+)\s*\)\s*\{'
        )
        sm = setter_pattern.match(stripped)
        if sm:
            setter_suffix = sm.group(1)
            param_name = sm.group(2)
            java_field = setter_suffix[0].lower() + setter_suffix[1:]
            if java_field.startswith('swig'):
                continue
            block = get_comment_block(lines, i)
            bean.methods.append(BeanMethod(
                kind="set", java_field=java_field,
                line_num=i, has_comment=block is not None,
                comment=block[0] if block else "",
                param_name=param_name,
            ))

    return bean if bean.fields else None


def resolve_mapping(java_name: str, struct: StructInfo,
                    convert_map: Optional[Dict[str, str]]) -> Optional[StructField]:
    """找到 Java 字段对应的 C++ 字段。"""
    if convert_map and java_name in convert_map:
        cpp_name = convert_map[java_name]
        for f in struct.fields:
            if f.name == cpp_name:
                return f
    for f in struct.fields:
        if f.name == java_name:
            return f
    for f in struct.fields:
        if snake_to_camel(f.name) == java_name:
            return f
    return None


def update_bean(bean: BeanInfo, struct: StructInfo,
                convert_map: Optional[Dict[str, str]], dry_run: bool) -> int:
    """给字段声明、getter、setter 补注释或替换不一致的注释。

    策略：
    - 无注释 → 插入 C++ 注释
    - 有注释且与 C++ 一致 → 跳过
    - 有注释但与 C++ 不一致 → 替换为 C++ 注释（C++ 是 ground truth）
    """
    with open(bean.file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    changes = 0
    # 统一收集所有操作，按行号降序执行（从后往前）
    # 操作类型: ("insert", line_num, new_line) 或 ("replace", start, end, new_line)
    operations = []

    def make_comment_line(cpp_comment: str, is_setter: bool, param_name: str = "") -> str:
        indent = "    "
        if is_setter and param_name:
            return f"{indent}/** @param {param_name} {cpp_comment} */\n"
        return f"{indent}/** {cpp_comment} */\n"

    # 字段声明
    for bf in bean.fields:
        cpp_field = resolve_mapping(bf.java_name, struct, convert_map)
        if not cpp_field or not cpp_field.comment:
            continue

        if not bf.has_comment:
            new_line = make_comment_line(cpp_field.comment, False)
            operations.append(("insert", bf.line_num, new_line))
            changes += 1
        elif bf.comment != cpp_field.comment:
            block = get_comment_block(lines, bf.line_num)
            if block:
                new_line = make_comment_line(cpp_field.comment, False)
                operations.append(("replace", block[1], block[2], new_line))
                changes += 1

    # getter / setter
    for m in bean.methods:
        cpp_field = resolve_mapping(m.java_field, struct, convert_map)
        if not cpp_field or not cpp_field.comment:
            continue

        is_setter = (m.kind == "set")
        # setter 的已有注释格式是 "@param xxx 注释"，提取注释部分比较
        if is_setter:
            existing = re.sub(r'^@param\s+\w+\s+', '', m.comment)
        else:
            existing = m.comment

        # setter 用字段名作为 @param 名，而非方法签名中的参数名（可能是 v）
        setter_param = m.java_field if is_setter else ""

        if not m.has_comment:
            new_line = make_comment_line(cpp_field.comment, is_setter, setter_param)
            operations.append(("insert", m.line_num, new_line))
            changes += 1
        elif existing != cpp_field.comment:
            block = get_comment_block(lines, m.line_num)
            if block:
                new_line = make_comment_line(cpp_field.comment, is_setter, setter_param)
                operations.append(("replace", block[1], block[2], new_line))
                changes += 1

    if not dry_run and operations:
        # 按行号降序执行，避免偏移
        for op in sorted(operations, key=lambda x: -(x[1])):
            if op[0] == "insert":
                _, line_num, new_line = op
                lines.insert(line_num, new_line)
            elif op[0] == "replace":
                _, start, end, new_line = op
                lines[start:end+1] = [new_line]

        with open(bean.file_path, 'w', encoding='utf-8') as f:
            f.writelines(lines)

    return changes


def main():
    parser = argparse.ArgumentParser(description='Sync comments from message.h to Java bean fields, getters, and setters')
    parser.add_argument('--message-h', required=True)
    parser.add_argument('--bean-dir', required=True)
    parser.add_argument('--convert', required=True)
    parser.add_argument('--dry-run', action='store_true')
    parser.add_argument('--target', help='Only process specific struct name')
    parser.add_argument('--exclude', default='', help='Comma-separated bean names to skip')
    args = parser.parse_args()

    exclude_set = set(s.strip() for s in args.exclude.split(',') if s.strip())

    print("=== Parsing message.h ===")
    structs = parse_message_h(args.message_h)
    print(f"  {len(structs)} structs")

    print("=== Parsing FlightHubSdkConvert.java ===")
    convert_mappings = parse_convert_file(args.convert)
    print(f"  {len(convert_mappings)} toNative mappings")

    if args.target:
        structs = {k: v for k, v in structs.items() if k == args.target}

    print("\n=== Processing handwritten beans ===")
    total = 0
    changes_total = 0

    for struct_name, struct in sorted(structs.items()):
        if struct_name in exclude_set:
            continue
        bean_file = os.path.join(args.bean_dir, f"{struct_name}.java")
        if not os.path.exists(bean_file):
            continue

        bean = parse_bean(bean_file)
        if not bean:
            continue

        total += 1
        convert_map = convert_mappings.get(struct_name)
        changes = update_bean(bean, struct, convert_map, args.dry_run)
        changes_total += changes

        if changes > 0:
            print(f"  {struct_name}.java: {changes} comments")

    print(f"\n  Processed {total} beans, {changes_total} comments added")
    print(f"  Mode: {'DRY RUN' if args.dry_run else 'APPLY'}")


if __name__ == '__main__':
    main()
