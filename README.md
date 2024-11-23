# Лабораторная работа №5. Хранение данных. Настройки и внешние файлы.
- Код приложения написан на языке Java и использует Android SDK.
- API: http://ntv.ifmo.ru/file/journal/идентификатор_журнала.pdf

## Инструкция по использованию приложения
Данное мобильное приложение позволяет пользователю асинхронно скачивать файлы журнала Научно-технический вестник. Файлы хранятся на сервере в формате PDF и расположены по адресу: `http://ntv.ifmo.ru/file/journal/идентификатор_журнала.pdf`
Не для всех ID имеются журналы, поэтому предусмотрено сообщение об отсутствии файла. В случае если файл не найден, ответ от сервера будет содержать главную страницу сайта.
<p align="center">
<img src="https://sun9-80.userapi.com/impg/p6NaFuHB0YQf2dY1e90NIRX5PPpBOoI7gif2uQ/RLgYwsoaDhY.jpg?size=720x1520&quality=95&sign=245a5a95656d911e6a4ae59a55339e26&type=album" width="250" height="500"> 
</p>

Существование файла опредляется по возвращаемому сервером заголовку (параметр `content-type`).
Файлы сохраняются на устройстве в папке, создаваемой при первом запуске приложения.
После окончания загрузки файла становится доступной кнопка «Смотреть» и кнопка «Удалить».
<p align="center">
<img src="https://sun9-18.userapi.com/impg/CONkksuEd_9oMJ46DKDb1WNxPDzaYzmGLWyKcg/ycgMSao8yko.jpg?size=720x1520&quality=95&sign=145e8308ed39d8214607e043a4733115&type=album" width="250" height="500"> 
</p>

При нажатии на кнопку «Смотреть» происходит открытие сохраненного на устройстве файла. Предусмотрена ошибка, если на устройстве не установлено приложение, открывающее PDF файлы.
<p align="center">
<img src="https://sun9-75.userapi.com/impg/iP7lVmuUP9bdEhWR2ZgPFjGvKiQ5rP9NVpJALQ/258xA8BeUmg.jpg?size=720x1520&quality=95&sign=593b6b632b5a3050002c52e6b9eeab93&type=album" width="250" height="500"> 
<img src="https://sun9-78.userapi.com/impg/5qDBOgu9wSMkqj_ynrKs8BZRvsPn2Mc31NpudA/P6Yo23L6tFA.jpg?size=720x1520&quality=95&sign=b7b06b1c9fcfb44ef62c2a0ea14d90ad&type=album" width="250" height="500"> 
</p>

При нажатии на кнопку «Удалить» загруженный файл удаляется с устройства.
<p align="center">
<img src="https://sun9-79.userapi.com/impg/JiWlCRnRHgi6DjXDsn11hRoXNtdkbwPnS8-p4g/NEY0YSZrJYk.jpg?size=720x1520&quality=95&sign=9e54f40aa5c27c65b31c48b4be420506&type=album" width="250" height="500"> 
</p>

При запуске приложения пользователю выводится всплывающее уведомление (`popupWindow`), с краткой инструкцией по использованию приложения, с чекбоксом «Больше не показывать» и кнопкой «ОК».
<p align="center">
<img src="https://sun9-38.userapi.com/impg/9e_YC09cGp94zPAhaaAfvgvDvzESx8Bq9EHkuA/IVa-93YIlBc.jpg?size=720x1520&quality=95&sign=761e690b178af1b58054def95153ab85&type=album" width="250" height="500"> 
</p>

Если чекбокс был отмечен и нажата кнопка ОК, необходимо произвести сохранение данного параметра используя `SharedPreferences`. При следующем запуске приложения производить проверку параметра, и не выводить всплывающее сообщение, если чекбокс был отмечен.
