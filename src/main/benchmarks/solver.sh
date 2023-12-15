JAR=lignes-claires-*-with-dependencies.jar
MAINCLASS=lignesclaires.LignesClaires

MAINARGS=`cat $1 | xargs`

java -server  -Xms512m -Xmx8192m -cp $JAR $MAINCLASS $MAINARGS $2

exit $?
