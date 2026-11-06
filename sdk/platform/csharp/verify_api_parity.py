"""Check generated business bridge coverage, not behavioral equivalence.

Container idioms and memory ownership helpers differ between Java and C#.
Compare native bridge names for SDK interfaces and callback entry points.
"""
import json
import re
import sys
from pathlib import Path


def verify(java_dir, csharp_dir):
    java_file = java_dir / 'flighthubsdkmoduleJNI.java'
    csharp_files = list(csharp_dir.glob('*PINVOKE.cs'))
    if not java_file.exists() or len(csharp_files) != 1:
        raise RuntimeError('Expected freshly generated Java JNI and one C# PInvoke class')
    java = java_file.read_text(encoding='utf-8')
    csharp = csharp_files[0].read_text(encoding='utf-8')
    java_methods = set(re.findall(r'\bnative\s+[\w.\[\]]+\s+(\w+)\(', java))
    csharp_methods = set(re.findall(r'\bextern\s+[\w.:\[\]]+\s+(\w+)\(', csharp))
    classes = {p.stem for p in java_dir.glob('*.java')
               if not p.stem.startswith('SWIGTYPE_')
               and p.stem not in {'flighthubsdkmoduleJNI', 'flighthubsdkmodule'}}
    missing_classes = sorted(name for name in classes if not (csharp_dir / (name + '.cs')).exists())
    # All SDK interface methods, getters, and callback onXxx entry points.
    expected = {m for m in java_methods
                if (re.match(r'(?:I[A-Z]\w*|FlightHubSdk)_', m) or 'Callback_on' in m)
                and not m.endswith(('_change_ownership', '_director_connect'))}
    missing_methods = sorted(expected - csharp_methods)
    if not expected or len(classes) < 100:
        raise RuntimeError('Reference is incomplete; refusing an empty parity check')
    return dict(java_classes=len(classes), checked_business_bridges=len(expected),
                missing_classes=missing_classes, missing_methods=missing_methods,
                scope='Generated class and business bridge names; live behavior needs integration tests')


if __name__ == '__main__':
    result = verify(Path(sys.argv[1]), Path(sys.argv[2]))
    output = Path(sys.argv[3])
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(result, ensure_ascii=False))
    sys.exit(bool(result['missing_classes'] or result['missing_methods']))
