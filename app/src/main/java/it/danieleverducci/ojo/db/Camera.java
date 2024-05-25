package it.danieleverducci.ojo.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Camera {

    @PrimaryKey
    @NonNull
    public String name;

    @ColumnInfo(name = "rtspUrl")
    public String rtspUrl;

    @ColumnInfo(name = "rtspHDUrl")
    public String rtspHDUrl;

    @ColumnInfo(name = "isEnabled")
    public boolean isEnabled = true;

    @Ignore
    public Camera(String name, String rtspUrl, String rtspHDUrl) {
        this.name = name;
        this.rtspUrl = rtspUrl;
        this.rtspHDUrl = rtspHDUrl;
    }

    public Camera() {
    }
}
