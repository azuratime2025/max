# Sample Data Files for Bulk Registration Testing

## 📁 Available Test Files:

### CSV Files:
- `students_basic.csv` - 10 famous scientists/artists with complete data
- `students_minimal.csv` - 5 philosophers/tech leaders with minimal data
- `students_with_errors.csv` - 7 composers with intentional errors for testing
- `students_sports.csv` - 10 athletes/coaches with sports data
- `students_large_batch.csv` - 25 writers/historical figures for bulk testing

## 🎯 How to Test Excel Support:

Since the app now supports both Excel and CSV files, you can:

1. **Use CSV files directly** - Copy any of the .csv files to your device
2. **Convert CSV to Excel** - Open any CSV file in Excel and save as .xlsx
3. **Create your own Excel file** with the same format

## 📋 Expected Format:

```
Student ID | Name | Class | Sub Class | Grade | Sub Grade | Program | Role
STU001 | Albert Einstein | Physics | Theoretical Physics | Grade 12 | Advanced | Science Program | Student
```

## 🚀 Testing Steps:

1. Copy test files to your Android device
2. Open the app → Register tab → "Bulk Registration from Excel/CSV"
3. Select either .xlsx, .xls, or .csv files
4. Test the import functionality

## ✅ What's Fixed:

- ✅ Excel files are now detected and provide helpful conversion instructions
- ✅ CSV files fully supported with robust parsing
- ✅ Clear guidance for Excel-to-CSV conversion
- ✅ Lightweight solution (no heavy dependencies)
- ✅ Fast build times and better app performance

## 📋 Excel to CSV Conversion:

If you have an Excel file (.xlsx or .xls):

1. **Open your Excel file**
2. **Click File → Save As**
3. **Choose "CSV (Comma delimited)" format**
4. **Save the file**
5. **Upload the CSV file to the app**

This ensures:
- ✅ Better compatibility across all devices
- ✅ Faster processing
- ✅ Smaller file sizes
- ✅ No dependency issues

The app will now guide you through this process when you select Excel files!
