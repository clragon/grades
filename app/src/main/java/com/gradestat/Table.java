package com.gradestat;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

import com.google.gson.*;

import org.threeten.bp.LocalDate;


public class Table implements Serializable {

    public Table(String name) {
        this.name = name;
    }

    public transient String saveFile = "";

    public String name;
    public double minGrade = 1;
    public double maxGrade = 6;

    public boolean useWeight = true;

    private transient final double fullWeight = 1.0;

    public double getFullWeight() {
        return fullWeight;
    }

    private List<Subject> Subjects = new ArrayList<>();

    public List<Subject> getSubjects() {
        return Subjects;
    }

    public Subject addSubject(String name) {
        Subject s = new Subject(name);
        s.parent = this;
        Subjects.add(s);
        return s;
    }

    public Subject movSubject(Subject s, int pos) {
        Subjects.remove(s);
        Subjects.add(pos, s);
        return s;
    }

    public void remSubject(Subject subject) {
        Subjects.remove(subject);
    }

    public boolean isValid() {
        for (Subject s : Subjects) {
            if (s.isValid()) {
                return true;
            }
        }
        return false;
    }

    public double getAverage(LocalDate before, LocalDate after) {
        if (!Subjects.isEmpty()) {
            double values = 0;
            ArrayList<Subject> excluded = new ArrayList<>();
            for (Subject s : Subjects) {
                double weighted = 0;
                for (Subject.Grade g : s.Grades) {
                    weighted += g.weight;
                }
                if (s.Grades.isEmpty() || weighted == 0 || s.getAverage(before, after) == 0) {
                    excluded.add(s);
                } else {
                    values += s.getAverage(before, after);
                }
            }
            return Math.round((values / (Subjects.size() - excluded.size())) * 2) / 2.0;
        } else {
            return 0;
        }
    }

    public double getAverage(LocalDate before) {
        return getAverage(before, getFirst());
    }

    public double getAverage() {
        return getAverage(getLast());
    }

    private LocalDate getEdgeDate(boolean last) {
        Subject edge;
        if (!this.Subjects.isEmpty()) {
            edge = this.Subjects.get(0);
            for (Subject s : this.Subjects) {
                if (last) {
                    if (s.getLast().isAfter(edge.getLast())) {
                        edge = s;
                    }
                } else {
                    if (s.getFirst().isBefore(edge.getFirst())) {
                        edge = s;
                    }
                }
            }
            if (last) {
                return edge.getLast();
            } else {
                return edge.getFirst();
            }
        } else {
            return LocalDate.now();
        }
    }

    public LocalDate getLast() {
        return getEdgeDate(true);
    }

    public LocalDate getFirst() {
        return getEdgeDate(false);
    }

    public double getCompensation(boolean twice) {
        double points = 0;
        if (!this.Subjects.isEmpty()) {
            for (Subject s : this.Subjects) {
                points += s.getCompensation(twice);
            }
        } else {
            points = 0;
        }
        return points;
    }

    public double getCompensation() {
        return getCompensation(true);
    }

    public class Subject implements Serializable {

        Subject(String name) {
            this.name = name;
        }

        public String name;

        private transient Table parent;

        public Table getParent() {
            return parent;
        }

        private List<Grade> Grades = new ArrayList<>();

        public List<Grade> getGrades() {
            return Grades;
        }

        public Grade addGrade(double value, double weight, String name, LocalDate creation) {
            Grade g = new Grade(value, weight);
            g.parent = this;
            g.name = name;
            g.creation = creation;
            Grades.add(g);
            return g;
        }

        public Grade addGrade(double value, double weight, String name) {
            return addGrade(value, weight, name, LocalDate.now());
        }

        public Grade addGrade(double value, double weight) {
            return addGrade(value, weight, String.format("%s %x", this.name, this.getGrades().size() + 1));
        }

        public Grade addGrade(double value) {
            return addGrade(value, fullWeight);
        }

        public Grade movGrade(Grade g, int pos) {
            Grades.remove(g);
            Grades.add(pos, g);
            return g;
        }

        public void remGrade(Grade g) {
            Grades.remove(g);
        }

        public boolean isValid() {
            for (Table.Subject.Grade g : Grades) {
                if (g.isValid()) {
                    return true;
                }
            }
            return false;
        }

        public double getAverage(LocalDate before, LocalDate after) {
            if (!Grades.isEmpty()) {
                double values = 0, weights = 0;
                for (Grade g : Grades) {
                    if (!(g.creation.isAfter(before) || g.creation.isBefore(after))) {
                        if (getParent().useWeight) {
                            values += (g.value * g.weight);
                            weights += g.weight;
                        } else {
                            values += (g.value * fullWeight);
                            weights += fullWeight;
                        }
                    }
                }
                return Math.round((values / weights) * 2) / 2.0;
            } else {
                return 0;
            }
        }

        public double getAverage(LocalDate before) {
            return getAverage(before, getFirst());
        }

        public double getAverage() {
            return getAverage(getLast());
        }

        private LocalDate getEdgeDate(boolean last) {
            if (!this.Grades.isEmpty()) {
                Grade edge = this.Grades.get(0);
                for (Grade g : this.Grades) {
                    if (last) {
                        if (g.creation.isAfter(edge.creation)) {
                            edge = g;
                        }
                    } else {
                        if (g.creation.isBefore(edge.creation)) {
                            edge = g;
                        }
                    }
                }
                return edge.creation;
            } else {
                return LocalDate.now();
            }
        }

        public LocalDate getLast() {
            return getEdgeDate(true);
        }

        public LocalDate getFirst() {
            return getEdgeDate(false);
        }

        public double getCompensation(boolean twice) {
            double points;
            if (getAverage() != 0) {
                points = getAverage() - 4;
                if (twice) {
                    if (points < 0) {
                        points = points * 2;
                    }
                }
            } else {
                points = 0;
            }
            return points;
        }

        public class Grade implements Serializable {
            Grade(double value, double weight) {
                this.value = value;
                this.weight = weight;
                this.creation = LocalDate.now();
            }

            public String name;
            public LocalDate creation;

            public double value;
            public double weight;

            private transient Subject parent;

            public Subject getParent() {
                return parent;
            }

            public boolean isValid() {
                if (weight == 0) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }


    public static Table read(String file) throws java.io.IOException {
        Table table;
        FileReader fileReader = new FileReader(file);
        table = new Gson().fromJson(fileReader, Table.class);
        table.saveFile = file;
        for (Subject s : table.Subjects) {
            s.parent = table;
            for (Subject.Grade g : s.Grades) {
                g.parent = s;
            }
        }
        return table;
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

    public boolean save() {
        try {
            write();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean delete() throws IllegalStateException {
        if (!this.saveFile.equals("")) {
            try {
                if (!new File(saveFile).delete()) {
                    return false;
                }
                Subjects = new ArrayList<>();
                name = "";
            } catch (Exception ex) {
                return false;
            }
        } else {
            throw new IllegalStateException();
        }
        return true;
    }
}
