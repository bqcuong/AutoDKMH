# AutoDKMH
*Command-line tool for automatically registering courses of VNU*

## Cập nhật 25/11/2018
Những phần mã nguồn quan trọng của tool đã bị lược bỏ, thay vào đó là các chỉ dẫn thực hiện. Đối tượng tool hướng đến
giúp đỡ là những người thực sự cần hoặc không đăng ký được. 

## Advantages
- Totally automatic tool
- Won't stop until successful registering or being killed by you
- Command-line based, you can run from a server which has strong network connection

## Setup
Config account and course codes in ```src/config.properties```
  - ```usr``` - Student code
  - ```passwd``` - Password
  - ```course_codes``` - List of course codes, separated by dot characters (```.```)
  - ```sleep_time``` - Sleep time between two adjacent executings
  
## Run
Run with maven plugin

```mvn install exec:java```
