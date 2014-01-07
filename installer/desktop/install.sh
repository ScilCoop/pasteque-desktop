execPath=$(readlink -f $(dirname $0))
java -jar $execPath/*.jar
