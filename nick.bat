call gradlew jar

mkdir output
java -DTRACE_READER=CANHACKER -Dmlq_file_name=output/nick.mlg                   -jar build/libs/can-log-tools-0.1-SNAPSHOT.jar  src/test/resources/vw_mqb_2010.dbc src/test/resources/med.canhacker

