// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <cstring>
#include "base64_codec.h"

namespace drc_sdk {
class Base64 {
 public:
  Base64();
  ~Base64();

  std::string Encode(const std::string &str);
  std::string Encode(const void *input, size_t length);
  char *Encode(const void *input, size_t input_length, size_t *output_length);

  std::string Decode(const std::string &str);
  std::string Decode(const void *input, size_t length);
  char *Decode(const void *input, size_t input_length, size_t *output_length);

  void set_alphabet(const char *alphabet);
  void set_padding(char padding);

  const char *alphabet() const;
  char padding() const;

 private:
  size_t indexof_(char ch, const char *alphabet);
  void destroy_reverse_alphabet_();
  bool check_decode_input_(const char *input, size_t length);
  char *encode_implement_(const char *input, size_t input_length, size_t *output_length);
  char *decode_implement_(const char *input, size_t input_length, size_t *output_length);

 private:
  static const char kDefaultPadding;
  static const char kDefaultAlphabet[64];
  static const signed char kDefaultReverseAlphabet[128];

 private:
  const char *alphabet_;
  const signed char *reverse_alphabet_;
  char padding_;
};

const char Base64::kDefaultPadding = '=';

const char Base64::kDefaultAlphabet[64] = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
    'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
    'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
    'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
    'w', 'x', 'y', 'z', '0', '1', '2', '3',
    '4', '5', '6', '7', '8', '9', '+', '/'
};

const signed char Base64::kDefaultReverseAlphabet[128] = {
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, 62, -1, -1, -1, 63,
    52, 53, 54, 55, 56, 57, 58, 59,
    60, 61, -1, -1, -1, -1, -1, -1,
    -1, 0, 1, 2, 3, 4, 5, 6,
    7, 8, 9, 10, 11, 12, 13, 14,
    15, 16, 17, 18, 19, 20, 21, 22,
    23, 24, 25, -1, -1, -1, -1, -1,
    -1, 26, 27, 28, 29, 30, 31, 32,
    33, 34, 35, 36, 37, 38, 39, 40,
    41, 42, 43, 44, 45, 46, 47, 48,
    49, 50, 51, -1, -1, -1, -1, -1
};

Base64::Base64()
    : alphabet_(kDefaultAlphabet),
      reverse_alphabet_(kDefaultReverseAlphabet),
      padding_(kDefaultPadding) {
}

Base64::~Base64() {
  destroy_reverse_alphabet_();
}

std::string Base64::Encode(const std::string &str) {
  return Encode(str.c_str(), str.size());
}

std::string Base64::Encode(const void *input, size_t length) {
  std::string result;
  if (char *out = Encode(input, length, nullptr)) {
    result.assign(out);
    delete[] out;
  }
  return result;
}

char *Base64::Encode(const void *input, size_t input_length, size_t *output_length) {
  return encode_implement_(static_cast<const char *>(input), input_length, output_length);
}

std::string Base64::Decode(const std::string &str) {
  return Decode(str.c_str(), str.size());
}

std::string Base64::Decode(const void *input, size_t length) {
  std::string result;
  size_t result_length;
  if (char *out = Decode(input, length, &result_length)) {
    result.assign(out, result_length);
    delete[] out;
  }
  return result;
}

char *Base64::Decode(const void *input, size_t input_length, size_t *output_length) {
  return decode_implement_(static_cast<const char *>(input), input_length, output_length);
}

void Base64::set_alphabet(const char *alphabet) {
  if (alphabet == nullptr) {
    alphabet = kDefaultAlphabet;
  }

  if (alphabet_ != alphabet) {
    alphabet_ = alphabet;
    destroy_reverse_alphabet_();
    if (alphabet_ == kDefaultAlphabet) {
      reverse_alphabet_ = kDefaultReverseAlphabet;
    } else {
      auto *reverse_alphabet = new signed char[128];
      memset(reverse_alphabet, -1, 128);
      for (size_t i = 0; i < 64; ++i) {
        char ch = alphabet_[i] & 0x7f;
        reverse_alphabet[size_t(ch)] = indexof_(ch, alphabet_);
      }
      reverse_alphabet_ = reverse_alphabet;
    }
  }
}

void Base64::set_padding(char padding) {
  padding_ = padding;
}

const char *Base64::alphabet() const {
  return alphabet_;
}

char Base64::padding() const {
  return padding_;
}

size_t Base64::indexof_(char ch, const char *alphabet) {
  const char *pos = strchr(alphabet, ch);
  return pos == nullptr ? 0 : (pos - alphabet);
}

void Base64::destroy_reverse_alphabet_() {
  if (reverse_alphabet_ != kDefaultReverseAlphabet) {
    delete[] reverse_alphabet_;
  }
}

bool Base64::check_decode_input_(const char *input, size_t length) {
  for (size_t i = 0; i < length; ++i) {
    if (reverse_alphabet_[size_t(input[i])] == static_cast<signed char>(-1)) {
      return false;
    }
  }
  return true;
}

char *Base64::encode_implement_(const char *input,
                                size_t input_length,
                                size_t *output_length) {
  if (input == nullptr) {
    return nullptr;
  }

  size_t encrypted_length = ((input_length + 2) / 3) << 2;

  // length overflow
  if (encrypted_length < input_length) {
    return nullptr;
  }

  if (output_length != nullptr) {
    *output_length = encrypted_length;
  }

  char *result = new char[encrypted_length + 1];
  char *out = result;
  size_t num_group = input_length / 3;
  for (size_t i = 0; i < num_group; ++i) {
    unsigned char a = input[0];
    unsigned char b = input[1];
    unsigned char c = input[2];
    *out++ = alphabet_[a >> 2];
    *out++ = alphabet_[((a & 0x03) << 4) | ((b & 0xf0) >> 4)];
    *out++ = alphabet_[((b & 0x0f) << 2) | ((c & 0xc0) >> 6)];
    *out++ = alphabet_[c & 0x3f];
    input += 3;
  }

  size_t num_padding = input_length % 3;
  if (num_padding == 1) {
    unsigned char a = input[0];
    *out++ = alphabet_[a >> 2];
    *out++ = alphabet_[(a & 0x03) << 4];
    *out++ = padding_;
    *out++ = padding_;
  } else if (num_padding == 2) {
    unsigned char a = input[0];
    unsigned char b = input[1];
    *out++ = alphabet_[a >> 2];
    *out++ = alphabet_[((a & 0x03) << 4) | (b >> 4)];
    *out++ = alphabet_[(b & 0x0f) << 2];
    *out++ = padding_;
  }

  *out = '\0';
  return result;
}

char *Base64::decode_implement_(const char *input,
                                size_t input_length,
                                size_t *output_length) {
  // input_length must be a multiple of 4
  if (input == nullptr || ((input_length & 0x03) != 0)) {
    return nullptr;
  }

  size_t num_padding = 0;
  for (const char *input_end = input + input_length;
       input_end != input && *--input_end == padding_;) {
    ++num_padding;
  }

  if (num_padding > 2) {
    return nullptr;
  }

  if (!check_decode_input_(input, input_length - num_padding)) {
    return nullptr;
  }

  size_t decrypted_length = (input_length >> 2) * 3 - num_padding;
  if (output_length != 0) {
    *output_length = decrypted_length;
  }

  char *result = new char[decrypted_length + 1];
  char *out = result;
  size_t num_group = decrypted_length / 3;
  for (size_t i = 0; i < num_group; ++i) {
    unsigned char a = reverse_alphabet_[size_t(input[0])];
    unsigned char b = reverse_alphabet_[size_t(input[1])];
    unsigned char c = reverse_alphabet_[size_t(input[2])];
    unsigned char d = reverse_alphabet_[size_t(input[3])];
    *out++ = (a << 2) | (b >> 4);
    *out++ = ((b & 0x0f) << 4) | (c >> 2);
    *out++ = ((c & 0x03) << 6) | d;
    input += 4;
  }

  if (num_padding == 1) {
    unsigned char a = reverse_alphabet_[size_t(input[0])];
    unsigned char b = reverse_alphabet_[size_t(input[1])];
    unsigned char c = reverse_alphabet_[size_t(input[2])];
    *out++ = (a << 2) | (b >> 4);
    *out++ = ((b & 0x0f) << 4) | (c >> 2);
  } else if (num_padding == 2) {
    unsigned char a = reverse_alphabet_[size_t(input[0])];
    unsigned char b = reverse_alphabet_[size_t(input[1])];
    *out++ = (a << 2) | (b >> 4);
  }

  *out = '\0';
  return result;
}

std::string base64_encode(const std::string &str) {
  Base64 b64;
  return b64.Encode(str);
}

std::string base64_encode(const void *input, size_t length) {
  Base64 b64;
  return b64.Encode(input, length);
}

std::string base64_decode(const std::string &str) {
  Base64 b64;
  return b64.Decode(str);
}

std::string base64_decode(const void *input, size_t length) {
  Base64 b64;
  return b64.Decode(input, length);
}
}  // namespace drc_sdk
