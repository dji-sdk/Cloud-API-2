#include "AIGCJson.hpp"
using namespace std;
using namespace aigc;

struct Config
{
    string name;
    std::optional<int> age;
    std::optional<string> desc;

    AIGC_JSON_HELPER(name, age, desc)
};

int main()
{
    Config cfg;

    // JSON 中 age 为 null，desc 有值
    bool ok = JsonHelper::JsonToObject(cfg, R"({"name":"test", "age":null, "desc":"hello"})");
    if (!ok)
        return 1;
    if (cfg.name != "test" || cfg.age.has_value() || !cfg.desc.has_value() || *cfg.desc != "hello")
        return 2;

    // JSON 中 age 有值，desc 为 null
    ok = JsonHelper::JsonToObject(cfg, R"({"name":"foo", "age":18, "desc":null})");
    if (!ok)
        return 3;
    if (cfg.name != "foo" || !cfg.age.has_value() || *cfg.age != 18 || cfg.desc.has_value())
        return 4;

    // 对象转 JSON：optional 无值时输出 null
    cfg.name = "bar";
    cfg.age = std::nullopt;
    cfg.desc = "world";
    string jsonStr;
    ok = JsonHelper::ObjectToJson(cfg, jsonStr);
    if (!ok)
        return 5;
    if (jsonStr.find("\"age\":null") == string::npos || jsonStr.find("\"desc\":\"world\"") == string::npos)
        return 6;

    return 0;
}
