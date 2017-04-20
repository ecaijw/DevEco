package com.example.caijingwei.devecocontact;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jxl.*;
import jxl.read.biff.BiffException;

/**
 * Created by caijingwei on 2017/4/19.
 */

public class ParseExcel {
    public static class Person {
        String name;
        String role;
        String number;
        String birthday;
    }

    private static final int COLUMN_NAME = 2;
    private static final int COLUMN_ROLE = 3;
    private static final int COLUMN_NUMBER = 4;
    private static final int COLUMN_BIRTHDAY = 5;
    public static ArrayList<Person> parse(Context context) throws IOException {
        ArrayList<Person> results = new ArrayList<>();
        String[] l = context.getResources().getAssets().list("./");
        for (int i = 0; i < l.length; ++i) {
            Log.d("asset", l[i]);
        }

        InputStream is = context.getResources().getAssets().open("DevEcoContact.xls");
        Workbook book = null;
        try {
            book = Workbook.getWorkbook(is);
            Sheet sheet = book.getSheet(0);
            int rows = sheet.getRows();
            // skip row#0, which is title row
            for (int i = 1; i < rows; ++i) {
                Person person = new Person();
                person.name = sheet.getCell(COLUMN_NAME, i).getContents();
                person.role = sheet.getCell(COLUMN_ROLE, i).getContents();
                person.number = sheet.getCell(COLUMN_NUMBER, i).getContents();
                person.birthday = sheet.getCell(COLUMN_BIRTHDAY, i).getContents();
                results.add(person);
                Log.d("excel", String.format("%s, %s, %s, %s", person.name, person.role, person.number, person.birthday));
            }
            book.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return results;
    }
}
