cmake_minimum_required(VERSION 3.18.1)
project("terminal")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c11 -Wall -Wextra -Werror -Os -fno-stack-protector")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--gc-sections")

add_library(terminal SHARED terminal.c)