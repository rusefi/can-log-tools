call gradlew jar

mkdir output
java -Dmlq_file_name=output/cl_revving.mlg                   -jar build/libs/can-log-tools-0.1-SNAPSHOT.jar opendbc/vw_golf_mk4.dbc ..\rusefi_documentation\OEM-Docs\VAG\2006-Passat-B6\passat-b6-stock-ecu-ecu-ptcan-parked-revving.trc
java -Dmlq_file_name=output/cl_gears.mlg                     -jar build/libs/can-log-tools-0.1-SNAPSHOT.jar opendbc/vw_golf_mk4.dbc ..\rusefi_documentation\OEM-Docs\VAG\2006-Passat-B6\passat-b6-stock-ecu-ecu-ptcan-parked-shifting-gears.trc
java -Dmlq_file_name=output/cl_running-pedal-up-and-down.mlg -jar build/libs/can-log-tools-0.1-SNAPSHOT.jar opendbc/vw_golf_mk4.dbc ..\rusefi_documentation\OEM-Docs\VAG\2006-Passat-B6\passat-b6-stock-ecu-ecu-ptcan-not-running-pedal-up-and-down.trc

