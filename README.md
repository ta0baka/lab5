# Лабораторная работа №5. Хранение данных. Настройки и внешние файлы.
Код приложения написан на языке Java и использует Android SDK.
API:http://ntv.ifmo.ru/file/journal/идентификатор_журнала.pdf

## Инструкция по использованию приложения
Данное мобильное приложение позволяет пользователю асинхронно скачивать файлы журнала "Научно-технический вестник". Файлы хранятся на сервере в формате PDF и расположены по адресу: `http://ntv.ifmo.ru/file/journal/идентификатор_журнала.pdf`
Не для всех ID имеются журналы, поэтому предусмотрено сообщение об отсутствии файла. В случае если файл не найден, ответ от сервера будет содержать главную страницу сайта.
Существование файла опредляется по возвращаемому сервером заголовку (параметр `content-type`).
Файлы сохраняются на устройстве в папке, создаваемой при первом запуске приложения.
После окончания загрузки файла становится доступной кнопка «Смотреть» и кнопка «Удалить».
При нажатии на кнопку «Смотреть» происходит открытие сохраненного на устройстве файла. Предусмотрена ошибка, если на устройстве не установлено приложение, открывающее PDF файлы.
При нажатии на кнопку «Удалить» загруженный файл удаляется с устройства.
При запуске приложения пользователю выводится всплывающее уведомление (`popupWindow`), с краткой инструкцией по использованию приложения, с чекбоксом «Больше не показывать» и кнопкой «ОК».
Если чекбокс был отмечен и нажата кнопка ОК, необходимо произвести сохранение данного параметра используя `SharedPreferences`. При следующем запуске приложения производить проверку параметра, и не выводить всплывающее сообщение, если чекбокс был отмечен.



```java
private void fetchCurrentSong() {
        new Thread(() -> {
            try {
                URL url = new URL("http://media.ifmo.ru/api_get_current_song.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String postData = "login=4707login&password=4707pass";
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("result").equals("success")) {
                    String info = jsonResponse.getString("info");
                    String[] parts = info.split(" – ");
                    String artist = parts[0];
                    String title = parts[1];

                    if (!lastSong.equals(title)) {
                        dbHelper.addSong(artist, title);
                        lastSong = title;
                        activity.runOnUiThread(activity::displaySongs);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in MyTask", e);
            }
        }).start();
    }
```
