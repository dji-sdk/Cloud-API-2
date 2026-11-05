// C# binding shares every Java bridge, template and callback definition.
// Linux x64 uses LP64: C++ long and unsigned long are 64 bits.
%include "stdint.i"
%apply long long { long };
%apply unsigned long long { unsigned long };
// C# reserves set_<property> for property accessors.
%rename(has_sn) flight_hub_sdk::LiveSyncListParams::set_sn;
%rename(has_schema) flight_hub_sdk::LiveSyncListParams::set_schema;
%rename(has_enable) flight_hub_sdk::LiveSyncListParams::set_enable;
%rename(has_enable) flight_hub_sdk::LiveSyncUpdateParams::set_enable;
%rename(has_schema) flight_hub_sdk::LiveSyncUpdateParams::set_schema;
%rename(has_schema_option) flight_hub_sdk::LiveSyncUpdateParams::set_schema_option;
%rename(has_converter_name) flight_hub_sdk::LiveSyncUpdateParams::set_converter_name;
%rename(has_sn) flight_hub_sdk::LiveSyncUpdateParams::set_sn;
%rename(has_camera_index) flight_hub_sdk::LiveSyncUpdateParams::set_camera_index;
%rename(has_video_type) flight_hub_sdk::LiveSyncUpdateParams::set_video_type;
%rename(has_video_quality) flight_hub_sdk::LiveSyncUpdateParams::set_video_quality;
%include "../../java/swig/flight_hub_sdk.i"
