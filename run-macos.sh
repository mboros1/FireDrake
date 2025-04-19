./mvnw clean package
java -XstartOnFirstThread --enable-native-access=ALL-UNNAMED -jar target/fire-drake-0.0.1-SNAPSHOT.jar
