// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "byte_buffer.h"
#include <sstream>
#include <iomanip>
#include <algorithm>
#include <cassert>
#include <cstring>
#include <utility>

namespace drc_sdk {
constexpr size_t kMallocUnit = 128;
ByteBuffer::ByteBuffer() {
  Init();
}

ByteBuffer::ByteBuffer(size_t n) {
  Init();
  Resize(n);
}

ByteBuffer::ByteBuffer(const void *buf, size_t n) {
  Init();
  Write(buf, n);
}

ByteBuffer::ByteBuffer(const ByteBuffer &buf) {
  Reset();
  Write(buf);
}

ByteBuffer::ByteBuffer(ByteBuffer &&buf) {
  Reset();
  buf_ = buf.buf_;
  capacity_ = buf.capacity_;
  size_ = buf.size_;
  rpos_ = buf.rpos_;
  wpos_ = buf.wpos_;
  buf.Init();
}

ByteBuffer::~ByteBuffer() {
  Reset();
}

void ByteBuffer::Reserve(size_t n) {
  if (n > capacity_) {
    size_t malloc_size = ((n + kMallocUnit) / kMallocUnit) * kMallocUnit;
    size_t inc_size = malloc_size - capacity_;
    size_t old_capacity = capacity_;
    capacity_ = malloc_size;
    buf_ = reinterpret_cast<char *>(realloc(buf_, malloc_size));
    memset(buf_ + old_capacity, 0, inc_size);
  }
}

void ByteBuffer::Resize(size_t n) {
  if (n >= size_) {
    Reserve(n);
    size_ = n;
  } else {
    size_ = n;
  }
}

size_t ByteBuffer::Size() const {
  return size_;
}

size_t ByteBuffer::Capacity() const {
  return capacity_;
}

bool ByteBuffer::Empty() const {
  return size_ == 0;
}

bool ByteBuffer::Eof() const {
  return rpos_ == size_;
}

bool ByteBuffer::Reof() const {
  return rpos_ == size_;
}

bool ByteBuffer::Weof() const {
  return wpos_ == size_;
}

ByteBuffer &ByteBuffer::Assign(const ByteBuffer &buf) {
  return Assign(buf.data(), buf.Size());
}

ByteBuffer &ByteBuffer::Assign(ByteBuffer &&buf) {
  if (&buf != this) {
    Reset();
    buf_ = buf.buf_;
    capacity_ = buf.capacity_;
    size_ = buf.size_;
    rpos_ = buf.rpos_;
    wpos_ = buf.wpos_;
    buf.Init();
  }
  return *this;
}

ByteBuffer &ByteBuffer::Assign(const void *buf, size_t n) {
  Clear();
  return Write(buf, n);
}

ByteBuffer &ByteBuffer::Write(const ByteBuffer &buf) {
  return Write(buf.data(), buf.Size());
}

ByteBuffer &ByteBuffer::Write(const void *buf, size_t n) {
  Reserve(wpos_ + n);
  memcpy(buf_ + wpos_, buf, n);
  wpos_ += n;
  size_ = std::max(wpos_, size_);
  return *this;
}

ByteBuffer &ByteBuffer::Write(char c) {
  return Write(&c, 1);
}

ByteBuffer &ByteBuffer::Write(size_t pos, const ByteBuffer &buf) {
  return Write(pos, buf.data(), buf.Size());
}

ByteBuffer &ByteBuffer::Write(size_t pos, const void *buf, size_t n) {
  Seekw(static_cast<int>(pos), seek_beg);
  return Write(buf, n);
}

size_t ByteBuffer::Read(ByteBuffer &buf, size_t n) {
  assert(rpos_ <= size_);
  size_t remain_count = size_ - rpos_;
  size_t read_count = std::min(remain_count, n);
  buf.Write(buf_ + rpos_, read_count);
  rpos_ += read_count;
  return read_count;
}

size_t ByteBuffer::Read(void *buf, size_t n) {
  assert(rpos_ <= size_);
  size_t remain_count = size_ - rpos_;
  if (remain_count == 0) {
    return 0;
  }
  size_t read_count = std::min(remain_count, n);
  memcpy(buf, buf_ + rpos_, read_count);
  rpos_ += read_count;
  return read_count;
}

size_t ByteBuffer::Read(size_t pos, ByteBuffer &buf, size_t n) {
  Seekr(static_cast<int>(pos), seek_beg);
  return Read(buf, n);
}

size_t ByteBuffer::Read(size_t pos, void *buf, size_t n) {
  Seekr(static_cast<int>(pos), seek_beg);
  return Read(buf, n);
}

void ByteBuffer::Seekr(int offset, seek_dir dir/*= seek_cur*/) {
  Seek(rpos_, offset, dir);
}

void ByteBuffer::Seekw(int offset, seek_dir dir/*= seek_cur*/) {
  Seek(wpos_, offset, dir);
}

size_t ByteBuffer::rpos() const {
  return rpos_;
}

size_t ByteBuffer::wpos() const {
  return wpos_;
}

size_t ByteBuffer::rmax() const {
  return size_ - rpos_;
}

size_t ByteBuffer::wmax() const {
  return size_ - wpos_;
}

char *const ByteBuffer::data() const {
  return buf_;
}

char *ByteBuffer::data() {
  return buf_;
}

const char *ByteBuffer::rptr() const {
  return buf_ + rpos_;
}

char *ByteBuffer::rptr() {
  return buf_ + rpos_;
}

const char *ByteBuffer::wptr() const {
  return buf_ + wpos_;
}

char *ByteBuffer::wptr() {
  return buf_ + wpos_;
}

void ByteBuffer::Attach(void *buf, size_t n) {
  Reset();
  buf_ = reinterpret_cast<char*>(buf);
  size_ = n;
  capacity_ = n;
}

void *ByteBuffer::Detach(size_t *n /*= nullptr*/) {
  char *buf = buf_;
  if (n != nullptr) {
    *n = size_;
  }
  Init();
  return buf;
}

void ByteBuffer::Swap(ByteBuffer &buf) {
  if (&buf != this) {
    std::swap(buf_, buf.buf_);
    std::swap(capacity_, buf.capacity_);
    std::swap(size_, buf.size_);
    std::swap(rpos_, buf.rpos_);
    std::swap(wpos_, buf.wpos_);
  }
}

void ByteBuffer::Reset() {
  if (buf_ != nullptr) {
    free(buf_);
  }
  Init();
}

void ByteBuffer::Clear() {
  size_ = 0;
  rpos_ = 0;
  wpos_ = 0;
}

ByteBuffer &ByteBuffer::operator=(const ByteBuffer &buf) {
  return Assign(buf);
}

ByteBuffer &ByteBuffer::operator=(ByteBuffer &&buf) {
  return Assign(std::move(buf));
}

ByteBuffer &ByteBuffer::operator+=(const ByteBuffer &buf) {
  return Write(buf);
}

void ByteBuffer::Init() {
  buf_ = nullptr;
  capacity_ = 0;
  size_ = 0;
  rpos_ = 0;
  wpos_ = 0;
}

void ByteBuffer::Seek(size_t &cur_pos, int offset, seek_dir dir) {
  switch (dir) {
    case seek_beg: SeekImpl(cur_pos, offset, 0);
      break;
    case seek_cur: SeekImpl(cur_pos, offset, static_cast<int>(cur_pos));
      break;
    case seek_end: SeekImpl(cur_pos, offset, static_cast<int>(size_));
      break;
    default: assert(0);
  }
}

void ByteBuffer::SeekImpl(size_t &cur_pos, int offset, int base_pos) {
  if (offset > 0) {
    cur_pos = std::min<size_t>(base_pos + offset, size_);
  } else {
    cur_pos = std::max(0, base_pos + offset);
  }
}

size_t ByteBuffer::size() const {
  return size_;
}

std::string ByteBuffer::ToString() const {
    auto* data = (unsigned char*)buf_;
    std::string result = "0x";
    std::string tmp;
    std::stringstream ss;
    for (int i = 0; i < size_; i++) {
        ss << std::hex << std::setfill('0') << std::setw(2) << (int)(data[i]) << " "<< std::endl;
        ss >> tmp;
        result += tmp;
    }
    return result;
}

}  // namespace drc_sdk
