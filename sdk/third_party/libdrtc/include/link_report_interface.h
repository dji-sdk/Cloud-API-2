#ifndef DRPC_LINK_REPORT_INTERFACE_H
#define DRPC_LINK_REPORT_INTERFACE_H


#ifdef __cplusplus
extern "C" {
#endif
void netlink_report_key_frame_lost(int32_t stream_id, bool key_frame_lost);

void netlink_report_bw_kbps(int32_t stream_id, int32_t bw_kbps);

#ifdef __cplusplus
}
#endif


#endif

