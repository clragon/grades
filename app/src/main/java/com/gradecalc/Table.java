package com.gradecalc;


import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.Serializable;

import com.google.gson.*;


public class Table implements Serializable {

    public Table() {
        new Table(this.name = "Fächer");
    }

    public Table(String name) {
        this.name = name;
    }

    public transient String saveFile = "";

    public String name;
    public double minGrade = 1;
    public double maxGrade = 6;

    private transient final double fullWeight = 1.0;

    private List<Subject> Subjects = new ArrayList<>();

    public List<Subject> getSubjects() {
        return Subjects;
    }

    public Subject addSubject(String name) {
        Subject s = new Subject(name);
        s.ownerTable = this;
        Subjects.add(s);
        return s;
    }

    public Subject addSubject(Subject s) {
        s.ownerTable = this;
        Subjects.add(s);
        return s;
    }

    public void remSubject(Subject subject) {
        Subjects.remove(subject);
    }

    public void remSubject(int index) {
        Subjects.remove(index);
    }

    public double getAverage() {
        if (!Subjects.isEmpty()) {
            double values = 0;
            for (Subject s : Subjects) {
                values += s.getAverage();
            }

            return Math.round((values / Subjects.size()) * 2) / 2.0;
        } else {
            return 0;
        }
    }

    public Date getLatest() {
        Subject latest;
        if (!this.Subjects.isEmpty()) {
            latest = this.Subjects.get(0);
            for (Subject s : this.Subjects) {
                if (s.getLatest().after(latest.getLatest())) {
                    latest = s;
                }
            }
            return latest.getLatest();
        } else {
            return new Date();
        }
    }

    public class Subject implements Serializable {

        Subject(String name) {
            this.name = name;
        }

        public String name;

        private transient Table ownerTable;

        public Table getOwnerTable() {
            return ownerTable;
        }

        public void setOwnerTable(Table newOwnerTable) {
            ownerTable.remSubject(this);
            newOwnerTable.addSubject(this);
        }

        private List<Grade> Grades = new ArrayList<>();

        public List<Grade> getGrades() {
            return Grades;
        }

        public Grade addGrade(double value, double weight, String name, Date creation) {
            Grade g = new Grade(value, weight);
            g.ownerSubject = this;
            g.name = name;
            g.creation = creation;
            Grades.add(g);
            return g;
        }

        public Grade addGrade(double value, double weight, String name) {
            return addGrade(value, weight, name, new Date());
        }

        public Grade addGrade(double value, double weight) {
            return addGrade(value, weight, String.format("%s %x", this.name, this.getGrades().size() + 1));
        }

        public Grade addGrade(double value) {
            return addGrade(value, fullWeight);
        }

        public Grade addGrade(Grade g) {
            g.ownerSubject = this;
            Grades.add(g);
            return g;
        }

        public void remGrade(Grade g) {
            Grades.remove(g);
        }

        public void remGrade(int index) {
            Grades.remove(index);
        }

        public double getAverage() {
            if (!Grades.isEmpty()) {
                double values = 0, weights = 0;
                for (Grade g : Grades) {
                    values += (g.value * g.weight);
                    weights += g.weight;
                }

                return Math.round((values / weights) * 2) / 2.0;
            } else {
                return 0;
            }
        }

        public Date getLatest() {
            if (!this.Grades.isEmpty()) {
                Grade latest = this.Grades.get(0);
                for (Grade g : this.Grades) {
                    if (g.creation.after(latest.creation)) {
                        latest = g;
                    }
                }
                return latest.creation;
            } else {
                return new Date();
            }
        }

        public class Grade implements Serializable {
            Grade(double value, double weight) {
                this.value = value;
                this.weight = weight;
            }

            public String name;

            public Date creation;

            public double value;

            public double weight;

            private transient Subject ownerSubject;

            public Subject getOwnerSubject() {
                return ownerSubject;
            }

            public void setOwnerSubject(Subject newOwnerSubject) {
                ownerSubject.remGrade(this);
                newOwnerSubject.addGrade(this);

            }

        }

    }


    public static Table read(String file) throws java.io.IOException {
        try {
            Table table;
            FileReader fileReader = new FileReader(file);
            table = new Gson().fromJson(fileReader, Table.class);
            table.saveFile = file;
            for (Subject s : table.Subjects) {
                s.ownerTable = table;
                for (Subject.Grade g : s.Grades) {
                    g.ownerSubject = s;
                }
            }
            return table;
        } catch (java.io.IOException ex) {
            throw ex;
        }
    }

    public void write(String file) throws java.io.IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(json);
        fileWriter.flush();
        fileWriter.close();
    }

    public void write() throws java.io.IOException, IllegalStateException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        if (!this.saveFile.equals("")) {
            FileWriter fileWriter = new FileWriter(this.saveFile);
            fileWriter.write(json);
            fileWriter.flush();
            fileWriter.close();
        } else {
            throw new IllegalStateException();
        }
    }
}
