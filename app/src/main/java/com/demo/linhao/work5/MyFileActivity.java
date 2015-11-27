package com.demo.linhao.work5;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Vector;

/**
 * Created by linhao on 15/11/24.
 */
public class MyFileActivity extends Activity {
    //支持的媒体格式
    private final String[]FILE_MapTable={
            ".3gp",".mov",".avi",".rmvb",".wmv",".mp3",".mp4"
    };
    private Vector<String> items = null;//items存放显示的名称
    private Vector<String> paths = null;//paths存放文件路径
    private Vector<String> sizes = null;//sizes存放文件大小
    private String rootPath = "/mnt/sdcard";//起始文件夹
    private EditText pathEditext; //路径
    private Button queryButton;//查询按钮
    private ListView fileListView;//文件列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("多媒体文件浏览");
        setContentView(R.layout.myfile);
        pathEditext = (EditText) findViewById(R.id.path_edit);
        queryButton = (Button) findViewById(R.id.qry_button);
        fileListView = (ListView) findViewById(R.id.file_listview);
        //单击查询点击事件
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(pathEditext.getText().toString());
                if (file.exists())
                {
                    if (file.isFile())
                    {
                        //如果是媒体文件则直接打开播放
                        openFile(pathEditext.getText().toString());
                    }else{
                        //如果是目录则打开目录下得文件
                        getFilesDir(pathEditext.getText().toString());
                    }
                }else{
                    Toast.makeText(MyFileActivity.this,"找不到该位置，请确定位置是否正确",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //设置ListItem中的文件被单击时要做的动作
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                fileOrDir(paths.get(i));
            }
        });
        //打开默认文件夹
        getFilesDir();
    }
    /**
     * 重写返回键功能：返回上一级文件夹
     *
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断触发案件是否为back键
        if (keyCode == KeyEvent.KEYCODE_BACK){
            pathEditext = (EditText) findViewById(R.id.path_edit);
            File file = new File(pathEditext.getText().toString());
            if (rootPath.equals(pathEditext.getText().toString().trim()))
            {
                return super.onKeyDown(keyCode,event);
            }else
            {
                getFilesDir(file.getParent());
                return true;
            }
            //如果不是back键则正常响应
        }else
        {
            return super.onKeyDown(keyCode, event);
        }

    }
    /**
     *  处理文件或目录的方法
     */
    private void fileOrDir(String path){
        File file = new File(path);
        if (file.isDirectory())
        {
            getFilesDir(file.getPath());
        }else{
            openFile(path);
        }
    }
    /**
     * 取得文件结构的方法
     */
    private void getFilesDir(String filePath){
        //设置目前所在的路径
        pathEditext.setText(filePath);
        items = new Vector<String>();
        paths = new Vector<String>();
        sizes = new Vector<String>();
        File f =  new File(filePath);
        File[] files = f.listFiles();
        if (files!=null)
        {
            //将所有文件添加到ArrayList中
            for (int i = 0;i<files.length;i++)
            {
                if (files[i].isDirectory())
                {
                    items.add(files[i].getName());
                    paths.add(files[i].getPath());
                    sizes.add("");
                }
            }
            for (int i = 0;i<files.length;i++)
            {
                if (files[i].isFile())
                {
                    String fileName = files[i].getName();
                    int index = fileName.lastIndexOf(".");
                    if (index>0)
                    {
                        String endName = fileName.substring(index,fileName.length()).toLowerCase();
                        String type = null;
                        for (int x = 0;x<FILE_MapTable.length;x++)
                        {
                            //符合预先定义的多媒体格式的文件才会再界面显示
                            if (endName.equals(FILE_MapTable[x]))
                            {
                                type = FILE_MapTable[x];
                                break;
                            }
                        }
                        if (type != null)
                        {
                            items.add(files[i].getName());
                            paths.add(files[i].getPath());
                            sizes.add(files[i].length()+"");
                        }
                    }
                }
            }
        }
        //使用自定义的FileListAdapter将数据传入ListView中

    }
    /*打开媒体文件*/
    private void openFile(String path)
    {
        //打开媒体播放器，进行播放
        Intent intent = new Intent(MyFileActivity.this,MediaPlayerActivity.class);
        intent.putExtra("path",path);
        startActivity(intent);
        finish();
    }
    /**
     * ListView列表适配器
     */
    class FileListAdapter extends BaseAdapter
    {
        private Vector<String> items = null;//存放显示的名称
        private MyFileActivity myFile;
        private FileListAdapter(MyFileActivity myFile,Vector<String> items){
            this.items = items;
            this.myFile = myFile;
        }
        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.elementAt(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertview, ViewGroup viewGroup) {
            if (convertview==null)
            {
                convertview = myFile.getLayoutInflater().inflate(R.layout.file_item,null);
            }
            //文件名称
            TextView name = (TextView) convertview.findViewById(R.id.name);
            //媒体文件类型
            ImageView music = (ImageView) convertview.findViewById(R.id.music);
            //文件类型
            ImageView folder = (ImageView) convertview.findViewById(R.id.folder);
            name.setText(items.elementAt(i));
            if (sizes.elementAt(i).equals(""))
            {
                //隐藏媒体文件图标，显示文件夹图标
                music.setVisibility(View.GONE);
                folder.setVisibility(View.VISIBLE);
            }else
            {
                folder.setVisibility(View.GONE);
                music.setVisibility(View.VISIBLE);
            }
            return convertview;
        }
    }
}
