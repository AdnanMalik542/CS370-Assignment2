package edu.cs;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ViewFilesServlet")
public class ViewFilesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Stored Files</title></head><body>");
        html.append("<h2>Files Stored in Database</h2>");
        html.append("<a href='index.html'>Back to Upload Page</a><br><br>");
        html.append("<table border='1' cellpadding='8'>");
        html.append("<tr><th>ID</th><th>File Name</th><th>Uploaded At</th><th>Content</th></tr>");

        String sql = "SELECT id, file_name, file_content, uploaded_at FROM uploaded_files ORDER BY id DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String fileName = rs.getString("file_name");
                String fileContent = rs.getString("file_content");
                String uploadedAt = rs.getString("uploaded_at");

                html.append("<tr>");
                html.append("<td>").append(id).append("</td>");
                html.append("<td>").append(escapeHtml(fileName)).append("</td>");
                html.append("<td>").append(escapeHtml(uploadedAt)).append("</td>");
                html.append("<td><pre>").append(escapeHtml(fileContent)).append("</pre></td>");
                html.append("</tr>");
            }

        } catch (Exception e) {
            html.append("<tr><td colspan='4'>Error: ")
                .append(escapeHtml(e.getMessage()))
                .append("</td></tr>");
            e.printStackTrace();
        }

        html.append("</table>");
        html.append("</body></html>");

        response.getWriter().write(html.toString());
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}