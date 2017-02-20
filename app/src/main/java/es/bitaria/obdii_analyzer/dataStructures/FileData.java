package es.bitaria.obdii_analyzer.dataStructures;

/**
 * Creado por Pedro Amador Diaz el 19/01/2017.
 */

public class FileData {
    public String name;
    public String data;
    public String size;

    public FileData(){
        super();
    }

    public FileData(String name, String size, String data){
        super();
        this.name = name;
        this.data = data;
        this.size = size;
    }
}
