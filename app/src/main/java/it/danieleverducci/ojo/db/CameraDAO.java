package it.danieleverducci.ojo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CameraDAO {
    @Query("SELECT * FROM Camera")
    List<Camera> getAll();

    @Query("SELECT * FROM Camera WHERE name == :name")
    Camera findByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Camera... cameras);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Camera camera);

    @Delete
    void delete(Camera camera);
}
