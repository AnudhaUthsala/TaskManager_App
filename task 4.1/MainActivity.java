//code of TodoModelDTO.java
package com.example.todoapp;

import com.j256.ormlite.field.DatabaseField;

public class TodoModelDTO {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    String title;

    @DatabaseField
    String description;
    @DatabaseField
    String date;

    @DatabaseField
    Boolean done;
    public TodoModelDTO(int id, String title, String description, String date,boolean done) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.done = done;
    }
    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }
    public TodoModelDTO() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

//code of ToDOAdapter.java

package com.example.todoapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ToDOAdapter extends RecyclerView.Adapter<ToDOAdapter.ItemViewHolder> {

    private Context context;
    private List<TodoModelDTO> todolist;
    private OrmliteHelper ormliteHelper;

    public ToDOAdapter(Context context, List<TodoModelDTO> todolist) {
        this.context = context;
        this.todolist = sortListByDate(todolist); // Sort the list by date
    }

    @NonNull
    @Override
    public ToDOAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.todoitem,parent,false);
        ormliteHelper = new OrmliteHelper(context);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDOAdapter.ItemViewHolder holder, int position) {
        TodoModelDTO todoModelDTO = todolist.get(position);
        holder.title.setText(todoModelDTO.getTitle());
        holder.date.setText(todoModelDTO.getDate());
        holder.description.setVisibility(View.GONE);
        holder.done.setVisibility(View.VISIBLE);
        holder.images.setVisibility(View.VISIBLE);

        holder.done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    todoModelDTO.setDone(true);
                    try {
                        ormliteHelper.update(TodoModelDTO.class, todoModelDTO);
                        RefreshItems();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(context, "Activity mark as done", Toast.LENGTH_SHORT).show();
                } else {
                    todoModelDTO.setDone(false);
                    try {
                        ormliteHelper.update(TodoModelDTO.class, todoModelDTO);
                        RefreshItems();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(context, "Activity mark as undone", Toast.LENGTH_SHORT).show();
                }
            }
        });


        holder.viewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                final View customLayout = inflater.inflate(R.layout.todoitem, null);

                TextView title       = customLayout.findViewById(R.id.title);
                TextView description = customLayout.findViewById(R.id.description);
                TextView date        = customLayout.findViewById(R.id.date);
                CheckBox done        = customLayout.findViewById(R.id.done);
                LinearLayout icons   = customLayout.findViewById(R.id.icons);

                description.setVisibility(View.VISIBLE);
                done.setVisibility(View.GONE);
                icons.setVisibility(View.GONE);

                title.setText(todoModelDTO.getTitle());
                description.setText(todoModelDTO.getDescription());
                date.setText(todoModelDTO.getDate());

                builder.setView(customLayout)
                        .setNegativeButton("OK", (dialog, id) -> dialog.cancel());

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        holder.editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                final View customLayout = inflater.inflate(R.layout.dialog_add_todo, null);

                EditText etDate = customLayout.findViewById(R.id.etTodoDate);
                EditText etTitle = customLayout.findViewById(R.id.etTodoTitle);
                EditText etDescription = customLayout.findViewById(R.id.etTodoDescription);

                etDate.setText(todoModelDTO.getDate());
                etTitle.setText(todoModelDTO.getTitle());
                etDescription.setText(todoModelDTO.getDescription());

                etDate.setInputType(InputType.TYPE_NULL);
                etDate.setOnClickListener(v -> {
                    final Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    new DatePickerDialog(context, (view1, year1, monthOfYear, dayOfMonth) -> {
                        String formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year1);
                        etDate.setText(formattedDate);
                    }, year, month, day).show();
                });

                builder.setView(customLayout)
                        .setPositiveButton("Add", (dialog, id) -> {
                            // Extract your data here from customLayout's EditTexts and handle as needed

                            String title = etTitle.getText().toString();
                            String description = etDescription.getText().toString();
                            String date = etDate.getText().toString();


                            // Check if any of the fields are empty
                            if (title.trim().isEmpty() || description.trim().isEmpty() || date.trim().isEmpty()) {
                                // Show an error message to the user
                                Toast.makeText(context, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                                return; // Stop further execution
                            }

                            //update object model
                            todoModelDTO.setTitle(title);
                            todoModelDTO.setDescription(description);
                            todoModelDTO.setDate(date);

                            try {
                                ormliteHelper.update(TodoModelDTO.class, todoModelDTO);
                                RefreshItems();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }

                        })
                        .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this item?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                try {
                                    ormliteHelper.deleteById(TodoModelDTO.class, todoModelDTO.getId());
                                }  catch (SQLException e) {
                                    Toast.makeText(context, "Can not Delete the Item", Toast.LENGTH_SHORT).show();
                                }
                                RefreshItems();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked No button, do nothing
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }
    private void RefreshItems() {
        try {
            MainActivity.todolist.clear(); // Clear the existing list
            MainActivity.todolist.addAll(ormliteHelper.getAll(TodoModelDTO.class)); // Add all items from the database
            notifyDataSetChanged(); // Notify adapter that data has changed
        } catch (SQLException e) {
            Toast.makeText(context, "Can not Load Items", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return todolist.size();
    }
    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        private TextView title;
        private ImageView viewIcon, editIcon, deleteIcon;
        private TextView date;
        private TextView description;
        private CheckBox done;
        private LinearLayout images;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title      = itemView.findViewById(R.id.title);
            viewIcon   = itemView.findViewById(R.id.view);
            editIcon   = itemView.findViewById(R.id.edit);
            deleteIcon = itemView.findViewById(R.id.delete);
            date       = itemView.findViewById(R.id.date);
            done       = itemView.findViewById(R.id.done);
            description= itemView.findViewById(R.id.description);
            images     = itemView.findViewById(R.id.icons);
        }
    }
    // Method to sort the list by date
    private List<TodoModelDTO> sortListByDate(List<TodoModelDTO> list) {
        Collections.sort(list, new Comparator<TodoModelDTO>() {
            @Override
            public int compare(TodoModelDTO o1, TodoModelDTO o2) {
                // Assuming date format is "dd/MM/yyyy"
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    Date date1 = format.parse(o1.getDate());
                    Date date2 = format.parse(o2.getDate());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0; // Return 0 for equality if parsing fails
                }
            }
        });
        return list;
    }
}


//code pf OrmliteHelper.java

package com.example.todoapp;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.List;

public class OrmliteHelper extends OrmLiteSqliteOpenHelper {

    public static final String DB_NAME = "todo_.db";
    private static final int DB_VERSION = 1;
    private Context context;
    // Public methods
    public OrmliteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
       getWritableDatabase();
    }
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, TodoModelDTO.class);
        } catch (SQLException | java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
    public <T> List getAll(Class clazz) throws SQLException, java.sql.SQLException {
        Dao<T, ?> dao = getDao(clazz);
        return dao.queryForAll();
    }
    public <T> Dao.CreateOrUpdateStatus createOrUpdate(T obj) throws SQLException, java.sql.SQLException {
        Dao<T, ?> dao = (Dao<T, ?>) getDao(obj.getClass());
        return dao.createOrUpdate(obj);
    }
    public <T> Dao<T, ?> getDaoFor(Class<T> clazz) throws SQLException, java.sql.SQLException {
        return getDao(clazz);
    }
    public <T> void deleteById(Class<T> clazz, int id) throws SQLException, java.sql.SQLException {
        Dao<T, Integer> dao = (Dao<T, Integer>) getDaoFor(clazz);
        dao.deleteById(id);
    }
    public <T> void update(Class<T> clazz, T obj) throws SQLException, java.sql.SQLException {
        Dao<T, Integer> dao = (Dao<T, Integer>) getDaoFor(clazz);
        dao.update(obj);
    }



}

//code of MainActivity.java
package com.example.todoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    public static ToDOAdapter toDOAdapter;
    private OrmliteHelper ormliteHelper;
    public static List<TodoModelDTO> todolist = new ArrayList<>();

    private FloatingActionButton floatingActionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ormliteHelper = new OrmliteHelper(this);

        recyclerView = findViewById(R.id.todolist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        floatingActionButton = findViewById(R.id.floatingActionButton);

        try {
            todolist = ormliteHelper.getAll(TodoModelDTO.class);
        } catch (SQLException e) {
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
        }
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View customLayout = inflater.inflate(R.layout.dialog_add_todo, null);

                EditText etDate = customLayout.findViewById(R.id.etTodoDate);
                etDate.setInputType(InputType.TYPE_NULL);
                etDate.setOnClickListener(v -> {
                    final Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    new DatePickerDialog(MainActivity.this, (view1, year1, monthOfYear, dayOfMonth) -> {
                        String formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year1);
                        etDate.setText(formattedDate);
                    }, year, month, day).show();
                });

                builder.setView(customLayout)
                        .setPositiveButton("Add", (dialog, id) -> {
                            // Extract your data here from customLayout's EditTexts and handle as needed
                            EditText etTitle = customLayout.findViewById(R.id.etTodoTitle);
                            EditText etDescription = customLayout.findViewById(R.id.etTodoDescription);
                            String title = etTitle.getText().toString();
                            String description = etDescription.getText().toString();
                            String date = etDate.getText().toString();


                            // Check if any of the fields are empty
                            if (title.trim().isEmpty() || description.trim().isEmpty() || date.trim().isEmpty()) {
                                // Show an error message to the user
                                Toast.makeText(MainActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                                return; // Stop further execution
                            }

                            //Create object model
                            TodoModelDTO todoModelDTO = new TodoModelDTO();
                            todoModelDTO.setTitle(title);
                            todoModelDTO.setDescription(description);
                            todoModelDTO.setDate(date);
                            todoModelDTO.setDone(false);

                            try {
                                ormliteHelper.createOrUpdate(todoModelDTO);
                                todolist = ormliteHelper.getAll(TodoModelDTO.class);
                                toDOAdapter = new ToDOAdapter(MainActivity.this,todolist);
                                recyclerView.setAdapter(toDOAdapter);
                                toDOAdapter.notifyDataSetChanged();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }

                        })
                        .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        toDOAdapter = new ToDOAdapter(this,todolist);
        recyclerView.setAdapter(toDOAdapter);
        toDOAdapter.notifyDataSetChanged();


    }

}