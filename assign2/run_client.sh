javac -cp "lib/json.jar" src/*.java -d .
java -cp .:lib/json.jar GameClient localhost 8000
