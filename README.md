[![Build Status](https://travis-ci.org/andreykirson/job4j_grabber.svg?branch=master)](https://travis-ci.org/andreykirson/job4j_grabber)
[![codecov](https://codecov.io/gh/andreykirson/job4j_grabber/branch/master/graph/badge.svg)](https://codecov.io/gh/andreykirson/job4j_grabber)



Система запускается по расписанию. 

Период запуска указывается в настройках - app.properties. 

Первый сайт будет sql.ru. В нем есть раздел job. 

Программа должна считывать все вакансии относящие к Java и записывать их в базу.

Доступ к интерфейсу через REST API.