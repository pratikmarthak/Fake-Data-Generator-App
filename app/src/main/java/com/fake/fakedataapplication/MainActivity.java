package com.fake.fakedataapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.github.javafaker.Faker;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button btnSelectDataTypes, btnGenerate, btnCopy, btnExportCSV, btnExportPDF;
    private TextView tvSelectedDataTypes, tvOutput;
    private EditText etQuantity;
    private ScrollView scrollView;
    private Faker faker;
    private List<String> selectedDataTypes;
    private List<List<String>> generatedData;

    private String[] dataTypes = {"Name", "Address", "Phone Number", "Email", "Credit Card"};
    private boolean[] checkedItems;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectDataTypes = findViewById(R.id.btnSelectDataTypes);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnCopy = findViewById(R.id.btnCopy);
        btnExportCSV = findViewById(R.id.btnExportCSV);
        btnExportPDF = findViewById(R.id.btnExportPDF);
        tvSelectedDataTypes = findViewById(R.id.tvSelectedDataTypes);
        etQuantity = findViewById(R.id.etQuantity);
        tvOutput = findViewById(R.id.tvOutput);
        scrollView = findViewById(R.id.sv);

        faker = new Faker();
        selectedDataTypes = new ArrayList<>();
        generatedData = new ArrayList<>();
        checkedItems = new boolean[dataTypes.length];

        btnSelectDataTypes.setOnClickListener(v -> showMultiSelectDialog());
        btnGenerate.setOnClickListener(v -> generateData());
        btnCopy.setOnClickListener(v -> copyToClipboard());
        btnExportCSV.setOnClickListener(v -> exportCSV());
        btnExportPDF.setOnClickListener(v -> exportPDF());

        checkPermissions();
    }

    private void showMultiSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Data Types");
        builder.setMultiChoiceItems(dataTypes, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedDataTypes.add(dataTypes[which]);
            } else {
                selectedDataTypes.remove(dataTypes[which]);
            }
        });
        builder.setPositiveButton("OK", (dialog, which) -> tvSelectedDataTypes.setText(String.join(", ", selectedDataTypes)));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void generateData() {
        String quantityText = etQuantity.getText().toString();
        if (quantityText.isEmpty()) {
            Toast.makeText(this, "Please enter a quantity!", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityText);
        if (quantity <= 0) {
            Toast.makeText(this, "Quantity must be greater than 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        generatedData.clear();
        StringBuilder output = new StringBuilder();

        output.append("+").append("-".repeat(20).repeat(selectedDataTypes.size())).append("+\n");

        output.append("| ");
        for (String header : selectedDataTypes) {
            output.append(String.format("%-18s | ", header));
        }
        output.append("\n");

        output.append("+").append("-".repeat(20).repeat(selectedDataTypes.size())).append("+\n");

        for (int i = 0; i < quantity; i++) {
            output.append("| ");
            List<String> row = new ArrayList<>();
            for (String type : selectedDataTypes) {
                String data;
                switch (type) {
                    case "Name":
                        data = faker.name().fullName();
                        break;
                    case "Address":
                        data = faker.address().fullAddress();
                        break;
                    case "Phone Number":
                        data = faker.phoneNumber().cellPhone();
                        break;
                    case "Email":
                        data = faker.internet().emailAddress();
                        break;
                    case "Credit Card":
                        data = faker.finance().creditCard();
                        break;
                    default:
                        data = "";
                        break;
                }
                row.add(data);
                output.append(String.format("%-18s | ", data));
            }
            generatedData.add(row);
            output.append("\n");
        }

        output.append("+").append("-".repeat(20).repeat(selectedDataTypes.size())).append("+\n");

        tvOutput.setText(output.toString());
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Generated Data", tvOutput.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void exportCSV() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Data");
        if (!directory.exists()) directory.mkdirs();

        String fileName = "FakeData_" + System.currentTimeMillis() + ".csv";
        File file = new File(directory, fileName);

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file));
            writer.writeNext(selectedDataTypes.toArray(new String[0]));

            for (List<String> row : generatedData) {
                writer.writeNext(row.toArray(new String[0]));
            }
            writer.close();

            Toast.makeText(this, "CSV saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportPDF() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Data");
        if (!directory.exists()) directory.mkdirs();

        String fileName = "FakeData_" + System.currentTimeMillis() + ".pdf";
        File file = new File(directory, fileName);

        try {
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            Table table = new Table(selectedDataTypes.size());

            for (String header : selectedDataTypes) {
                table.addCell(new Cell().add(new Paragraph(header).setBold()).setBorder(new SolidBorder(1)));
            }

            for (List<String> row : generatedData) {
                for (String cellData : row) {
                    table.addCell(new Cell().add(new Paragraph(cellData)).setBorder(new SolidBorder(1)));
                }
            }

            document.add(table);
            document.close();

            Toast.makeText(this, "PDF saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
}

