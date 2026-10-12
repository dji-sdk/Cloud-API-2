#include "AIGCJson.hpp"
using namespace std;
using namespace aigc;

struct NumericTypes
{
    int8_t i8;
    uint8_t u8;
    int16_t i16;
    uint16_t u16;
    int32_t i32;
    uint32_t u32;
    int64_t i64;
    uint64_t u64;

    AIGC_JSON_HELPER(i8, u8, i16, u16, i32, u32, i64, u64)
};

int main()
{
    NumericTypes obj;
    bool ok = JsonHelper::JsonToObject(obj, R"({"i8":-1,"u8":255,"i16":-32768,"u16":65535,"i32":-2147483648,"u32":4294967295,"i64":-9223372036854775808,"u64":18446744073709551615})");
    if (!ok)
        return 1;
    if (obj.i8 != -1 || obj.u8 != 255 || obj.i16 != -32768 || obj.u16 != 65535)
        return 2;
    if (obj.i32 != -2147483648 || obj.u32 != 4294967295)
        return 3;
    if (obj.i64 != -9223372036854775807LL - 1 || obj.u64 != 18446744073709551615ULL)
        return 4;

    string jsonStr;
    ok = JsonHelper::ObjectToJson(obj, jsonStr);
    if (!ok)
        return 5;

    NumericTypes obj2;
    ok = JsonHelper::JsonToObject(obj2, jsonStr);
    if (!ok)
        return 6;
    if (obj2.i8 != obj.i8 || obj2.u8 != obj.u8 || obj2.i16 != obj.i16 || obj2.u16 != obj.u16)
        return 7;
    if (obj2.i32 != obj.i32 || obj2.u32 != obj.u32 || obj2.i64 != obj.i64 || obj2.u64 != obj.u64)
        return 8;

    return 0;
}
