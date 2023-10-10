export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.

while IFS= read -r line; do
  java -jar ReGaSol.jar $line
done < "tests"



