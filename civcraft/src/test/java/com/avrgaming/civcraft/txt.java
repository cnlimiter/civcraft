package com.avrgaming.civcraft;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class txt {

    public static void main(String[] args) throws Exception {
        List<String> list = new ArrayList<>(1000);
        String originName = "C:\\temp\\zh_lang.yml";
        Files.lines(Paths.get("D:\\jd.txt"), StandardCharsets.UTF_8).count();
        try (Reader inputStreamReader = new InputStreamReader(new FileInputStream(new File(originName)), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(inputStreamReader)) {
            String line = "";
            while ((line = br.readLine()) != null) {
                list.add(line.trim());
            }
        }
        String targetName = "C:\\temp\\zh_lang_new.yml";
        list.sort((s1, s2) -> {
            s1 = s1.startsWith("#") ? (s1.startsWith("# ")?s1.substring(2):s1.substring(1)) : s1;
            s2 = s2.startsWith("#") ? (s2.startsWith("# ")?s2.substring(2):s2.substring(1)) : s2;
            return s1.compareTo(s2);
        });
        try (Writer outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(targetName)), StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(outputStreamWriter)) {
            for (String s : list) {
                if (StringUtils.isNotEmpty(s)) {
                    bw.write(s);
                    bw.newLine();
                }
            }
        }

    }
}
