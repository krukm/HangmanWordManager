package com.matthewkruk.jh5_wordmanager_mkruk;

public interface FileOperations {

    public void newFile(String category);

    public void open(String category);

    public void delete(String category);

    public void replaceWord(int index, String value);
}
