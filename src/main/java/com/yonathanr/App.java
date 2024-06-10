package com.yonathanr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class App {
    static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static String previousIp = "0.0.0.0";

    private static String[] hosts = null;
    private static String domainName = null;
    private static String ddnsPassword = null;
    private static Long timerSchedule = null;

    public static void main(String[] args) {

        for (String arg : args) {
            if (arg.startsWith("-hosts=")) {
                hosts = arg.substring(7).split(",");
            } else if (arg.startsWith("-domain_name=")) {
                domainName = arg.substring(13);
            } else if (arg.startsWith("-ddns_password=")) {
                ddnsPassword = arg.substring(15);
            } else if (arg.startsWith("-timer_schedule=")) {
                timerSchedule = Long.valueOf(arg.substring(16));
            }
        }

        // Verificar que se hayan proporcionado todos los argumentos necesarios
        if (hosts == null || domainName == null || ddnsPassword == null || timerSchedule == null) {
            System.err.println("Uso: java -jar app.jar -hosts=<host1,host2,...> -domain_name=<nombre_de_dominio> -ddns_password=<contraseña> -timer_schedule=<milisegundos>" +
                    "\nEjemplo: java -jar app.jar -hosts='AAA,BBB,CCC' -domain_name=example.com -ddns_password=12345678 -timer_schedule=30000");
            System.exit(1);
        }

        System.out.println(formatter.format(new Date()) + " - Se inicia dynamic-dns-namecheap con IP temporal: " + previousIp);
        System.out.println("- Hosts: " + String.join(", ", hosts));
        System.out.println("- Domain Name: " + domainName);
        System.out.println("- DDNS Password: **********");
        System.out.println("- Timer Schedule: " + timerSchedule);

        Timer timer = new Timer();
        timer.schedule(new CheckIpTask(), 0, timerSchedule);
    }

    static class CheckIpTask extends TimerTask {
        @Override
        public void run() {
            try {
                String currentIp = getCurrentIp();
                if (Boolean.FALSE.equals(currentIp.equals(previousIp))) {
                    System.out.println(formatter.format(new Date()) + " - Se inicia actualización de IP de: " + previousIp + " a " + currentIp);
                    actualizarIpEnNamecheap(hosts, domainName, ddnsPassword, currentIp);
                    System.out.println(formatter.format(new Date()) + " - Se actualizarón los DNS con la IP: " + currentIp);
                    previousIp = currentIp;
                } else {
                    System.out.println(formatter.format(new Date()) + " - IP actual: " + currentIp);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("ERROR: " + e.getMessage());
            }
        }

        private void actualizarIpEnNamecheap(String[] hosts, String domain, String password, String ip) {
            for (String host : hosts) {
                String url = String.format("https://dynamicdns.park-your-domain.com/update?host=%s&domain=%s&password=%s&ip=%s",
                        host, domain, password, ip);

                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (200 == response.statusCode()) {
                        System.out.println(formatter.format(new Date()) + " - Se actualizo la IP: " + ip + " en el host: " + host + " correctamente.");
                    } else {
                        System.err.println(formatter.format(new Date()) + " - Ocurrió un error al actualizar la IP: " + ip + " en el host: " + host);
                    }
                } catch (IOException | InterruptedException e) {
                    System.err.println("ERROR: " + e.getMessage());
                }
            }
        }

        private String getCurrentIp() throws IOException, InterruptedException {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.ipify.org/?format=json"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);
            return jsonNode.get("ip").asText();
        }
    }
}

