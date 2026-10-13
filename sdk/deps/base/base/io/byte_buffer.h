// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdlib>
#include <string>
#include <memory>  // 必须包含此头文件
namespace drc_sdk {

enum seek_dir {
  seek_beg,
  seek_cur,
  seek_end
};

class ByteBuffer {
 public:
  ByteBuffer();
  explicit ByteBuffer(size_t n);
  ByteBuffer(const void *buf, size_t n);
  ByteBuffer(const ByteBuffer &buf);
  ByteBuffer(ByteBuffer && buf);
  ~ByteBuffer();
  void Reserve(size_t n);
  void Resize(size_t n);
  size_t Size() const;
  size_t size() const;
  size_t Capacity() const;
  bool Empty() const;
  bool Eof() const;
  bool Reof() const;
  bool Weof() const;
  ByteBuffer &Assign(const ByteBuffer &buf);
  ByteBuffer &Assign(ByteBuffer &&buf);
  ByteBuffer &Assign(const void *buf, size_t n);
  ByteBuffer &Write(const ByteBuffer &buf);
  ByteBuffer &Write(char c);
  ByteBuffer &Write(const void *buf, size_t n);
  ByteBuffer &Write(size_t pos, const ByteBuffer &buf);
  ByteBuffer &Write(size_t pos, const void *buf, size_t n);
  template<typename T>
  ByteBuffer &WritePod(const T &v) {
    return Write((const void *)&v, sizeof(v));
  }
  template<typename T>
  ByteBuffer &WritePod(size_t pos, const T &v) {
    return Write(pos, (const void *)&v, sizeof(v));
  }
  size_t Read(ByteBuffer &buf, size_t n);
  size_t Read(void *buf, size_t n);
  size_t Read(size_t pos, ByteBuffer &buf, size_t n);
  size_t Read(size_t pos, void *buf, size_t n);
  template<typename T>
  size_t ReadPod(T &v) {
    return Read(reinterpret_cast<void *>(&v), sizeof(v));
  }
  template<typename T>
  size_t ReadPod(size_t pos, T &v) {
    return Read(pos, reinterpret_cast<void *>(&v), sizeof(v));
  }
  void Seekr(int offset, seek_dir dir = seek_cur);
  void Seekw(int offset, seek_dir dir = seek_cur);
  size_t rpos() const;
  size_t wpos() const;
  size_t rmax() const;
  size_t wmax() const;
  char *const data() const;
  char *data();
  const char *rptr() const;
  char *rptr();
  const char *wptr() const;
  char *wptr();
  void Attach(void *buf, size_t n);
  void *Detach(size_t *n = nullptr);
  void Swap(ByteBuffer &buf);
  void Reset();
  void Clear();
  ByteBuffer &operator=(const ByteBuffer &buf);
  ByteBuffer &operator=(ByteBuffer &&buf);
  ByteBuffer &operator+=(const ByteBuffer &buf);

  std::string ToString() const;
 private:
  void Init();
  void Seek(size_t &cur_pos, int offset, seek_dir dir);
  void SeekImpl(size_t &cur_pos, int offset, int base_pos);

  char *buf_{};
  size_t capacity_{};
  size_t size_{};
  size_t rpos_{};
  size_t wpos_{};
};
}  // namespace drc_sdk
