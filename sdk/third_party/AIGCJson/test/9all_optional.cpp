#include "AIGCJson.hpp"
using namespace std;
using namespace aigc;

struct AllOptional
{
    optional<string> name;
    optional<int> age;

    AIGC_JSON_HELPER_ALL_OPTIONAL(name, age)
};

int main()
{
    AllOptional obj;
    bool ok = JsonHelper::JsonToObject(obj, R"({"name":"test"})");
    if (!ok)
        return 1;
    if (!obj.name.has_value() || *obj.name != "test" || obj.age.has_value())
        return 2;

    obj.age = 18;
    obj.name = nullopt;
    string jsonStr;
    ok = JsonHelper::ObjectToJson(obj, jsonStr);
    if (!ok)
        return 3;
    if (jsonStr.find("\"name\"") != string::npos)
        return 4;
    if (jsonStr.find("\"age\":18") == string::npos)
        return 5;

    return 0;
}
