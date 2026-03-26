package edu.cs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/FileUploadServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1,   // 1 MB threshold
    maxFileSize = 1024 * 1024 * 2,         // 2 MB max per file
    maxRequestSize = 1024 * 1024 * 5       // 5 MB max request
)
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/index.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        try {
            String applicationPath = request.getServletContext().getRealPath("");
            String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;

            File fileSaveDir = new File(uploadFilePath);
            if (!fileSaveDir.exists()) {
                fileSaveDir.mkdirs();
            }

            Part filePart = request.getPart("fileName");

            if (filePart == null || filePart.getSize() == 0) {
                response.getWriter().write("<h2>Upload Rejected</h2>");
                response.getWriter().write("<p>No file selected.</p>");
                return;
            }

            String fileName = new File(filePart.getSubmittedFileName()).getName();
            String lowerName = fileName.toLowerCase();

            if (!(lowerName.endsWith(".txt") || lowerName.endsWith(".csv")
                    || lowerName.endsWith(".xml") || lowerName.endsWith(".html")
                    || lowerName.endsWith(".java"))) {
                response.getWriter().write("<h2>Upload Rejected</h2>");
                response.getWriter().write("<p>Only text files (.txt, .csv, .xml, .html, .java) are supported.</p>");
                return;
            }

            String fullPath = uploadFilePath + File.separator + fileName;
            filePart.write(fullPath);

            File uploadedFile = new File(fullPath);
            String content = Files.readString(uploadedFile.toPath(), StandardCharsets.UTF_8);

            saveToDatabase(fileName, content);

            response.getWriter().write("<h2>Upload Successful</h2>");
            response.getWriter().write("<p><b>File:</b> " + escapeHtml(fileName) + "</p>");
            response.getWriter().write("<p><b>File Size:</b> " + uploadedFile.length() + " bytes</p>");
            response.getWriter().write("<p><b>Saved to database successfully.</b></p>");
            response.getWriter().write("<h3>File Content:</h3>");
            response.getWriter().write("<pre>" + escapeHtml(content) + "</pre>");
            response.getWriter().write("<br><a href='index.html'>Back to Upload Page</a>");
            response.getWriter().write("<br><a href='ViewFilesServlet'>View Files Stored in Database</a>");

        } catch (IllegalStateException e) {
            response.getWriter().write("<h2>Upload Rejected</h2>");
            response.getWriter().write("<p>File is too large. Maximum allowed file size is 2 MB.</p>");
        } catch (Exception e) {
            response.getWriter().write("<h2>Error</h2>");
            response.getWriter().write("<p>" + escapeHtml(e.getMessage()) + "</p>");
            e.printStackTrace();
        }
    }

    private void saveToDatabase(String fileName, String content) throws Exception {
        String sql = "INSERT INTO uploaded_files (file_name, file_content) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fileName);
            ps.setString(2, content);
            ps.executeUpdate();
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}