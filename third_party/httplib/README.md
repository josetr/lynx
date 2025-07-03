Name: cpp-httplib
URL: https://github.com/yhirose/cpp-httplib
License: MIT
License FILE: LICENSE
Revision: ea850cbfa74e2dff228c49bf94542ce5331d73b5

Local Modifications:

- Split header into separate .h and .cc files
- Added BUILD.gn
- Fixed some clang-tidy warnings
- Removed server-related logic
- In httplib.cc at line 4851: Forbidden redirect to a different host when using HTTP
