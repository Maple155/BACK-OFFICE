package com.test.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.test.test.Personne;
import com.test.test.User;
import annotation.Controller;
import annotation.FileParam;
import annotation.Get;
import annotation.GetURL;
import annotation.JSON;
import annotation.MapParam;
import annotation.Param;
import annotation.Post;
import annotation.RequestMapping;
import service.ModelView;

@Controller
public class ControllerA {

    @Get
    @GetURL(url = "/controllerA")
    public ModelView test1page() {
        ModelView model = new ModelView("controllerA.jsp");
        model.addObject("test", "Hello");
        model.addObject("test2", "Word");
        return model;
    }

    @Get
    @GetURL(url = "/testAPI")
    public ModelView testAPIpage() {
        ModelView model = new ModelView("test-api.jsp");
        model.addObject("test", "Hello");
        model.addObject("test2", "Word");
        return model;
    }

    @JSON
    @Get
    @GetURL(url = "/api/user")
    public User getUser() {
        User user = new User();
        user.setName("John");
        user.setPrenom("Doe");
        user.setAge(25);
        user.setDateNaissance(Date.valueOf("1998-05-15"));
        user.setHobbies(new String[] { "Tennis", "Lecture", "Coding" });

        List<String> diplomes = new ArrayList<>();
        diplomes.add("Licence Informatique");
        diplomes.add("Master Data Science");
        user.setDiplome(diplomes);

        return user;
    }

    @JSON
    @Get
    @GetURL(url = "/api/personne")
    public Personne getPersonne() {
        Personne p = new Personne();
        p.setName("Alice");
        p.setPrenom("Smith");
        p.setAge(30);
        p.setDateNaissance(Date.valueOf("1993-08-20"));
        p.setHobbies(new String[] { "Yoga", "Peinture" });

        List<String> diplomes = new ArrayList<>();
        diplomes.add("BAC+5");
        p.setDiplome(diplomes);

        return p;
    }

    @JSON
    @Get
    @GetURL(url = "/api/data")
    public ModelView getData() {
        ModelView model = new ModelView("ignored.jsp");
        model.addObject("message", "Success");
        model.addObject("count", 42);

        List<User> users = new ArrayList<>();
        User u1 = new User();
        u1.setName("Bob");
        u1.setAge(28);
        users.add(u1);

        model.addObject("users", users);
        return model;
    }

    @JSON
    @Get
    @GetURL(url = "/api/users")
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        User u1 = new User();
        u1.setName("Alice");
        u1.setAge(25);
        users.add(u1);

        User u2 = new User();
        u2.setName("Bob");
        u2.setAge(30);
        users.add(u2);

        return users;
    }

    @JSON
    @Get
    @GetURL(url = "/api/message")
    public String getMessage() {
        return "Hello World";
    }

    @JSON
    @Get
    @GetURL(url = "/api/count")
    public int getCount() {
        return 42;
    }

    @JSON
    @Post
    @GetURL(url = "/api/user/create")
    public User createUser(User user) {
        System.out.println("User créé: " + user);
        return user;
    }

    @Get
    @GetURL(url = "/controllerA/instance/page")
    public ModelView instancePage() {
        ModelView model = new ModelView("instance.jsp");
        return model;
    }

    @RequestMapping
    @GetURL(url = "/controllerA/instance")
    public String instanceTest(User user, Personne personne) {
        return user.toString() + " \n " + personne.toString();
    }

    @RequestMapping
    @GetURL(url = "/controllerA/instance/param")
    public String instanceParamTest(@Param("user") User user, @Param("personne") Personne personne) {
        return user.toString() + " \n " + personne.toString();
    }

    @RequestMapping
    @GetURL(url = "/controllerA/map")
    public String mapTest(@MapParam Map<String, Object> testMap) {
        StringBuilder result = new StringBuilder();
        result.append("<h3>Parametres reçus :</h3>");

        for (Map.Entry<String, Object> entry : testMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            result.append("<p><strong>").append(key).append(":</strong> ");

            if (value instanceof String[]) {
                String[] values = (String[]) value;
                if (values.length > 0) {
                    result.append("[");
                    for (int i = 0; i < values.length; i++) {
                        if (values[i] != null && !values[i].isEmpty()) {
                            result.append(values[i]);
                            if (i < values.length - 1) {
                                result.append(", ");
                            }
                        }
                    }
                    result.append("]");
                } else {
                    result.append("Aucune valeur");
                }
            } else {
                result.append(value != null ? value.toString() : "null");
            }

            result.append("</p>");
        }

        return result.toString();
    }

    @Get
    @GetURL(url = "/upload-form")
    public ModelView uploadForm() {
        ModelView model = new ModelView("upload.jsp");
        return model;
    }

    @Post
    @GetURL(url = "/upload")
    public String handleFileUpload(@FileParam Map<String, byte[]> files,
            @Param("description") String description) {
        StringBuilder result = new StringBuilder();
        result.append("<h2>Upload reussi!</h2>");
        result.append("<p>Description: ").append(description).append("</p>");
        result.append("<h3>Fichiers reçus:</h3><ul>");

        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            String fileName = entry.getKey();
            byte[] fileData = entry.getValue();
            result.append("<li>").append(fileName)
                    .append(" (").append(fileData.length).append(" bytes)</li>");
        }

        result.append("</ul>");
        return result.toString();
    }

    @JSON
    @Post
    @GetURL(url = "/api/upload")
    public Map<String, Object> handleApiFileUpload(@FileParam Map<String, byte[]> files) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("filesCount", files.size());

        List<Map<String, Object>> filesList = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("name", entry.getKey());
            fileInfo.put("size", entry.getValue().length);
            filesList.add(fileInfo);
        }

        response.put("files", filesList);
        return response;
    }
}