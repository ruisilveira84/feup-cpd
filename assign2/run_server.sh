javac -cp "lib/json.jar" src/*.java -d .
java -cp .:lib/json.jar GameServer 8000
