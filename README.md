# Translator
## Инструкция по запуску 
1. Склонируйте проект с помощью команды:
 * git clone https://github.com/Novia2003/Translator.git
2. Перейдите в папку проекта:
 * cd Translator/
3. Выполните команду:
* ./gradlew clean build -x test
4. Запустите docker контейнеры:
 * docker-compose up
5. Перейдите по сскылке:
 * http://localhost:8080/swagger-ui/index.html#/
6. На случай непредвиденных проблем я запустил проект на облачном сервере, поэтому можно воспользоваться им:
 * http://176.57.220.218:8080/swagger-ui/index.html#/

## Как использовать
### Перевод текста
* Выберите /api/v1/translate
* Нажмите кнопку "Try it out"
* Заполните поля:
   
  - text (исходный текст),

  - sourceLanguageCode (код языка исходного текста),

  - targetLanguageCode (код конечного языка)
* Нажмите кнопку "Execute"
### Просмотр списка кодов доступных языков
* Выберите /api/v1/languages
* Нажмите кнопку "Try it out"
* Нажмите кнопку "Execute"
## Решение возможных проблем при запуске
### Не установлен Docker:
* sudo apt update
* sudo apt upgrade
* sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common
* curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
* sudo apt-key fingerprint 0EBFCD88
* sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
* sudo apt-get update
* sudo apt-get install -y docker-ce
### Не установлен Docker-compose
* apt  install docker-compose
### Не установлены переменные среды JAVA_HOME
* sudo apt install openjdk-17-jdk
### Файлу gradlew не выданы права на выполнение
* chmod +x gradlew
