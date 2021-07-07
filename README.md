[TOC]

# 小组分工

116072019021 叶婧雯：添加查看编辑笔记、时间戳、刷新、查看个人信息
116072019004 王林韵：主题设置、插入图片
116072019008 王雯：模糊搜索、UI美化
116072019025 曾家霖：注册、登录、修改密码、隔离、定时提醒、侧滑删除和收藏



# 引言

本App借鉴并分析了现有的备忘录应用的特点与功能，结合现有的开源UI组件设计了一个轻量的备忘录App。主要工作包括：

(1) 设计登录、注册、主页浏览、编辑笔记、菜单、主题等界面的UI。

(2) 实现同一笔记内文字和图片等信息可持久化保存的功能。

(3) 实现对于笔记的收藏、定时提醒、查询、编辑、删除、增加等功能。

(4) 对于上述功能，根据不同用户进行“隔离”，使得未登录的用户无法删除和修改其他用户的笔记，保证隐私。

(5) 结合现有的组件，实现搜索笔记的功能。

(6) 保存用户的偏好设置（是否记住密码、是否收藏笔记），提高用户使用的便捷性。

(7) 实现主题的更换以及夜间模式自动切换的功能。



# 结构

app内主要的几个包文件：

1.assets文件夹
·litePal.xml写入需创建的表的结构
本App需要两个表，分别是保存用户信息的User表和保存备忘笔记的Data表
2.bean文件夹  用以litepal创建数据库，这里的信息类指定的数据库的格式和结构。
存放两个表示表的结构的Java文件，这将被litepal.xml用作定义信息类
3.dao文件夹  包含数据操作所使用的DAO
4.activity文件夹 界面相关
5.ui文件夹 是网上找的开源ui组件
6.adapter文件夹     以Apdater的方式使用Data
7.service文件夹   定时提醒
8.util文件夹    是额外的自定义的类，用来存放全局信息，以及解决问题的小工具
· CommonUtility里有 重新定义图片大小函数 和 生成随机数函数
· UriToPathUtil 图片路径转换 根据Uri获取图片绝对路径
· BitmapToPathUtil  随机生产文件名并保存bitmap（位图）到本地
· AppGlobal 存放全局信息类 用来标识

# 数据库设计
**数据库设计图**

![1](C:\Users\16588\Desktop\picture\1.png)

用户信息表  User
字段名	数据类型	可空	字段描述
UID	INTEGER	否	主键ID，自增长
ImagePath	VARCHAR2(64)	是	头像存储位置
Nickname	VARCHAR2(64)	否	用户昵称
Password	VARCHAR2(64)	否	登陆密码
Number	VARCHAR2(64)	否	唯一KEY，手机号码

 备忘数据表  Data
字段名	数据类型	可空	字段描述
UID	INTEGER	否	主键ID，自增长
Content	TEXT	否	笔记正文
Date	TEXT	否	创建日期
Exacttime	INTEGER	否	最后编辑时间
Pending	INTEGER	否	标记是否待办，在实际中未使用
Reminder	INTEGER	否	标记是否设置了提醒
Star	INTEGER	否	标记是否收藏
User_id	INTEGER	是	外键，所属User的uid，如果是未登录状态下创建则为空

# 主页
Activity：MainActivity
页面展示：**主页展示**

![2](C:\Users\16588\Desktop\picture\2.png)

主要功能：主页展示全部的备忘，点击左上角可以滑出菜单，最下方是一个“增加”按钮，页面上方是App名称，下方是搜索框，用户可以进行模糊搜索和下拉刷新。

**自动切换主题为暗黑模式:**主页是用户进入App的第一个页面，所以在创建时会判断当前时间是否为晚上6点到凌晨6点，如果是则自动切换主题为暗黑模式。

```java
//自动切换暗黑主题关键代码
super.onCreate(savedInstanceState);
        //live是静态的防止多次设定，支持修改主题，如果每次进入都根据时间写死为dark 那后面修改主题就不行了
        if (live == 0 && (timeStatus() == 5 || timeStatus() == 1)) {
            AppGlobal.THEME = "dark";
            live = 1;
            //setTheme(R.style.DarkTheme);
        }
```

**添加新的备忘录:**主页下方的“增加”按钮通过widget.FloatingActionButton实现

```java
/*
*在activity_main.xml中的
*FloatingActionButton布局
*/
<android.support.design.widget.FloatingActionButton
            android:id="@+id/fab"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center|bottom"
            android:layout_margin="10dp"
            android:background="@drawable/list_item_bg_white"
            android:contentDescription="@string/todo"
            android:src="@drawable/add"
            app:backgroundTint="#00cec9"
            app:elevation="15dp"
            fab:fab_addButtonColorNormal="#fab1a0"
            fab:fab_addButtonColorPressed="#d63031"
            fab:fab_addButtonPlusIconColor="@color/white" />
```

点击之后会进入“查看备忘”的界面，只是查看的是一个空备忘。

```java
//FloatingActionButton行为代码
       /*
         * set the main btn for Adding.
         * 添加新的备忘录
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MemorandumActivity.class);
                //to memorandumActivity.class
                v.getContext().startActivity(intent);
            }
        });
```

**模糊搜索:**对于搜索框，依靠SearchView实现，由于SearchView未提供修改文本输入光标及文字颜色属性的接口，在实际开发有修改光标及文字颜色的需求，在这里通过反射机制来实现。

```java
//利用反射实现搜索栏主题更新
searchView = (SearchView) findViewById(R.id.search_view);
searchView.clearFocus();

int searchPlateId = searchView.getContext().getResources()
        .getIdentifier("android:id/search_plate", null, null);
int searchMagId = searchView.getContext().getResources()
        .getIdentifier("android:id/search_mag_icon", null, null);
int searchClsId = searchView.getContext().getResources()
        .getIdentifier("android:id/search_close_btn", null, null);

View searchPlate = searchView.findViewById(searchPlateId);
ImageView searchMagIcon = (ImageView) this.findViewById(searchMagId);
ImageView searchClsIcon = (ImageView) this.findViewById(searchClsId);
if (searchMagIcon != null && "dark".equals(AppGlobal.THEME)) {
    searchMagIcon.setColorFilter(Color.WHITE);
}
if (searchClsIcon != null && "dark".equals(AppGlobal.THEME)) {
    searchClsIcon.setColorFilter(Color.WHITE);
}
if (searchPlate != null) {
    int searchTextId = searchPlate.getContext().getResources()
            .getIdentifier("android:id/search_src_text", null, null);
    TextView searchText = (TextView) searchPlate.findViewById(searchTextId);

    if (searchText != null) {
        searchText.setTextSize(14);
        if ("dark".equals(AppGlobal.THEME)) {
            searchText.setTextColor(Color.WHITE);
            searchText.setHintTextColor(Color.WHITE);
            try {
                Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                mCursorDrawableRes.setAccessible(true);
            } catch (Exception ignored) {

            }
        }
    }
```

与此同时还需要为SearchView设置一个文本监听，当搜索框内的文本发生改变时离开搜索相应的备忘。还需要根据当前选择的分类来进行相应的数据库的查询。

```java
//搜索栏的文本监听
searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
            User currentUser = UserDAO.findUser(AppGlobal.USERNAME);
            int userId = UserDAO.findUserId(AppGlobal.USERNAME);
            Log.i(TAG, Integer.toString(userId));
            if (!TextUtils.isEmpty(newText.trim())) {
                //判定searchview的输入框是否非空
                dataList.clear();
                if (getTitle() == "Just R0's Note") {
                    dataList = dataDAO.queryUserData(newText.trim(), userId);
                } else if (getTitle() == "Remind") {
                    dataList = dataDAO.queryUserReminder(newText.trim(), userId);
                } else if (getTitle() == "Favorites") {
                    dataList = dataDAO.queryUserFavorites(newText.trim(), userId);
                }
            } else {
                dataList.clear();
                if (getTitle() == "Remind") {
                    dataList = dataDAO.getUserReminder(userId);
                } else if (getTitle() == "Favorites") {
                    dataList = dataDAO.getUserFavorites(userId);
                } else {
                    dataList = dataDAO.getUserData(userId);
                }
            }
        } else {
            if (!TextUtils.isEmpty(newText)) {
                dataList.clear();
                if (getTitle() == "Just R0's Note") {
                    dataList = dataDAO.queryData(newText.trim());
                } else if (getTitle() == "Remind") {
                    dataList = dataDAO.queryReminder(newText.trim());
                } else if (getTitle() == "Favorites") {
                    dataList = dataDAO.queryFavorites(newText.trim());
                }
            } else {
                if (getTitle() == "Remind") {
                    dataList = dataDAO.getUserReminder();
                } else if (getTitle() == "Favorites") {
                    dataList = dataDAO.getFavorites();
                } else {
                    dataList = dataDAO.getData();
                }
            }
        }
        adapter = new DataAdapter(dataList);
        recyclerView.setAdapter(adapter);
        return false;
    }
});
```

```java
//搜索栏模糊搜索的示例代码
public List<Data> queryData(String newText) {
    dataList = DataSupport.order("exactTime desc").where("content like ?", "%" + newText + "%").find(Data.class);
    return dataList;
}
```

**刷新组件:**
![9](C:\Users\16588\Desktop\picture\9.png)

主页的主体布局没有采用scrollView，而是使用SwipeRefreshLayout，这将为我们自带一个刷新控件。在这里需要为其指定一个“刷新动作”的监听器。

```java
//刷新控件的使用
SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
    public void onRefresh() {
        refreshData();
    }
};
swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
swipeRefresh.setOnRefreshListener(listener);
```

使用OnRefreshListener完成主页的刷新，用户向下滑动即可刷新页面。
由于不希望刷新动作阻塞App当前的运行，造成用户使用感受上的卡顿，所以在响应刷新行为的时候使用一个额外的线程异步完成数据的更新、页面UI重绘等工作。
```java
//刷新控件关键代码，使用线程异步执行
private void refreshData() {
    new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataList.clear();
                    if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
                        int userId = UserDAO.findUserId(AppGlobal.USERNAME);
                        if (getTitle() == "Favorites") {
                            initUserFavorites(userId);
                        } else {
                            initUserMemo(userId);
                        }
                    } else {
                        if (getTitle() == "Favorites") {
                            initFavorites();
                        } else {
                            initMemo();
                        }
                    }
                    swipeRefresh.setRefreshing(false);
                }
            });
        }
    }).start();
}
```
**“隔离”**

对于数据，首先需要判断是否为已登录的状态，如果是则从数据库中查询对应User的id的全部备忘数据，如果未登录则查询全部备忘后输出。之后将数据List设为主页页面的adapter。

对于显示在主页的每一项都设置了一个左滑动作，可以对备忘进行收藏和删除。

![3](C:\Users\16588\Desktop\picture\3.png)

![4](C:\Users\16588\Desktop\picture\4.png)



 

这个UI是依靠“liyi”开源的SwipeItemLayout框架实现的，使用的时候需要将他指定给recyclerView的addOnItemTouchListener方式以使用。还需要在对应item的adapter的布局文件中写入，以及在对应Java代码中实现点击的行为。

而在这里，未登录用户是不能改变某一特定用户的备忘的收藏状态的，也不能删除某一特定用户创建的备忘。但如果备忘本来就是由未登录用户创建的，未登录用户就可以收藏和删除。

```java
//Adapter中实现隔离
case R.id.favorite: {
    int chooseStar = data.getStar();
    Message message = new Message();

    if (AppGlobal.USERNAME.equals("")) {
        if (cur_user_id == -1) {
            // Toast.makeText(v.getContext(), "Of course...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(v.getContext(), "Go away !", Toast.LENGTH_SHORT).show();
            break;
        }
    }
    if (chooseStar == 2) {
        data.setStar(1);
        data.update(id);
        chooseStar = data.getStar();
        Log.d(TAG, String.valueOf(chooseStar));
        favorite.setText("Like it !!");
        favorite.setBackgroundResource(R.drawable.btn_mark);
        Toast.makeText(v.getContext(), "No...", Toast.LENGTH_SHORT).show();
        message.what = favoriteNo;
        //发送favoriteNo的消息
    } else {
        data.setStar(2);
        data.update(id);
        chooseStar = data.getStar();
        Log.d(TAG, String.valueOf(chooseStar));
        favorite.setText("No...");
        favorite.setBackgroundResource(R.drawable.btn_mark_grey);
        Toast.makeText(v.getContext(), "Like it !!", Toast.LENGTH_SHORT).show();
        message.what = favoriteYes;
        //发送favoriteYes的消息
    }
    handler.sendMessage(message);
    //使用handler发送消息
    break;
}
```

# Adapter

在主页创建之后会查询数据库中适合的备忘，并以此构造Adapter。同时还需要实现点击元素的行为代码。在此使用handler异步接收并处理信息，根据信息的不同调用不同的功能（更换ui），这么做的目的是防止改变UI时页面的闪烁。

```java
//handler接收消息并处理
private Handler handler = new Handler() {
    @SuppressLint("HandlerLeak")
    public void handleMessage(@NonNull Message message) {
        //handler接收并处理消息，根据不同case决定收藏button的样式和文字
        switch (message.what) {
            case favoriteNo:
                favorite.setText("Like this !!");
                favorite.setBackgroundResource(R.drawable.btn_mark);
                break;
            case favoriteYes:
                favorite.setText("No...");
                favorite.setBackgroundResource(R.drawable.btn_mark_grey);
                break;
            default:
                favorite.setText("Like this !!");
                favorite.setBackgroundResource(R.drawable.btn_mark);
                break;
        }
    }
};
```

而对于“收藏”和“删除”功能也是在这里实现。比如点击“收藏”之后，除了更新数据库外，还会使用handler发送消息异步改变UI。

```java
//具体动作代码
case R.id.favorite: {
    int chooseStar = data.getStar();
    Message message = new Message();

    if (AppGlobal.USERNAME.equals("")) {
        if (cur_user_id == -1) {
            // Toast.makeText(v.getContext(), "Of course...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(v.getContext(), "Go away !", Toast.LENGTH_SHORT).show();
            break;
        }
    }
    if (chooseStar == 2) {
        data.setStar(1);
        data.update(id);
        chooseStar = data.getStar();
        Log.d(TAG, String.valueOf(chooseStar));
        favorite.setText("Like it !!");
        favorite.setBackgroundResource(R.drawable.btn_mark);
        Toast.makeText(v.getContext(), "No...", Toast.LENGTH_SHORT).show();
        message.what = favoriteNo;
        //发送favoriteNo的消息
    } else {
        data.setStar(2);
        data.update(id);
        chooseStar = data.getStar();
        Log.d(TAG, String.valueOf(chooseStar));
        favorite.setText("No...");
        favorite.setBackgroundResource(R.drawable.btn_mark_grey);
        Toast.makeText(v.getContext(), "Like it !!", Toast.LENGTH_SHORT).show();
        message.what = favoriteYes;
        //发送favoriteYes的消息
    }
    handler.sendMessage(message);
    //使用handler发送消息
    break;
}
```

未登录用户取消未登录用户的收藏，成功

![5](C:\Users\16588\Desktop\picture\5.png)

未登录用户收藏特定用户的备忘，失败

![6](C:\Users\16588\Desktop\picture\6.png)

**Glide加载图片:**备忘的右下角会显示收藏状态、备忘状态，在这里使用使用Glide向imageview中加载图片。Glide是一个快速高效的Android图片加载库,注重于平滑的滚动。

```java
//使用Glide加载图片
private void showStar(ViewHolder holder, int currentStar) {

        if (currentStar == 2) {
            Glide.with(context).load(R.drawable.star).asBitmap().into(holder.dataStar);
        } else {
//            Glide.with(context).load(R.drawable.star_plain).asBitmap().into(holder.dataStar);
//            holder.dataStar.setBackgroundResource(0);
            holder.dataStar.setWillNotDraw(true);
        }
    }
```

**数据部分:**主页点击对应的备忘之后会跳转到对应的查看、编辑界面，这个过程先需要获取点击的位置，然后获取对应的Data，关键信息是id、content、date等数据，之后将这些数据都put到intent中，跳转到新的界面。

```java
//Adapter传递信息的部分代码
@Override
public void onClick(View v) {
    int position = getAdapterPosition();
    //根据用户点击获取当前位置
    Data data = mDataList.get(position);
    int cur_user_id = 0;
    User cur_user = data.getUser();
    if (cur_user != null) {
        cur_user_id = cur_user.getId();
    } else {
        cur_user_id = -1;
    }
    Log.d("ID", String.valueOf(cur_user_id));
    //将当前位置传入对应的data对象
    int id = data.getId();
    favorite = (Button) itemView.findViewById(R.id.favorite);
    switch (v.getId()) {
        case R.id.main:
            String date = data.getDate();
            String content = data.getContent();
            String imagePath = data.getImagePath();

            intent.setClass(v.getContext(), MemorandumActivity.class);
            intent.putExtra("id", id);
            //向intent中传入当前对象的属性的值
            intent.putExtra("content", content);
            intent.putExtra("date", date);
            intent.putExtra("data", data);
            if (imagePath != null && !imagePath.equals("")) {
                intent.putExtra("imagePath", imagePath);
            }
            v.getContext().startActivity(intent);
            break;
```

其中的mDataList，会在主页载入的时候查询数据库之后被构建妥当。

```java
//mDataList构建的部分代码
f (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
    int userId = UserDAO.findUserId(AppGlobal.USERNAME);
    dataList = dataDAO.getUserData(userId);
} else {
    dataList = dataDAO.getData();
}
final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
LinearLayoutManager layoutManager = new LinearLayoutManager(this);

recyclerView.setLayoutManager(layoutManager);
adapter = new DataAdapter(dataList);
```

# 主页导航

布局文件：menu.nav_menu.xml，layout.nav_header.xml

页面展示：主页导航栏

![7](C:\Users\16588\Desktop\picture\7.png)

导航栏依靠widget.NavigationView实现，这个控件需要指定一个menu布局来使用。

```java
/*
*activity_main.xml
*主页导航栏实现
*/
    <android.support.design.widget.NavigationView
        android:id="@+id/nav_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/nav_menu" />
```

如果是未登录的状态，用户点击头像之后会跳转到登陆界面。如果是已登录状态，顶部信息栏则会显示用户的昵称以及头像。

 已登录用户的信息栏

![8](C:\Users\16588\Desktop\picture\8.png)



点击导航栏中对应的项则会跳转到对应的页面。而“Settings”页面需要登录才能开启，如果未登录，点击后则会跳转到登陆界面。


```java
/*
*activity_main.xml
*主页导航栏实现
*/
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        // headerImage = (CircleImageView) headerView.findViewById(R.id.icon_image);
        headerImage = (ImageView) headerView.findViewById(R.id.icon_image);
        signIn = (TextView) headerView.findViewById(R.id.sign_in);
        TextView signComment = (TextView) headerView.findViewById(R.id.sign_comment);
        if (!AppGlobal.USERNAME.equals("")) {
            String currentImagePath = UserDAO.findImagePath(AppGlobal.USERNAME);
            Glide.with(this).load(currentImagePath).asBitmap().into(headerImage);
            signIn.setText(AppGlobal.NAME);
            signComment.setText("Welcome");
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }
```

# 查看、编辑页面

Activity：MemorandumActivity

布局文件：MemorandumActivity.xml

页面展示：

**查看**

未登录用户创建的备忘

![10](C:\Users\16588\Desktop\picture\10.png)

某一用户创建的备忘

![11](C:\Users\16588\Desktop\picture\11.png)

本页面的最上方显示了备忘的归属者。如果是未登录用户创建的，则会显示Public Note；如果是某一特定用户创建的则会显示用户的昵称。

此界面从主页进入，主页会向intent中放入必要的信息，而在此界面加载时则需要取出并展示（如果有）。

```java
//查看界面数据
currentId = intent.getIntExtra("id", 0);
        currentContent = intent.getStringExtra("content");
        currentDate = intent.getStringExtra("date");
        currentTime = intent.getStringExtra("exactTime");
        Log.e(TAG, Integer.toString(intent.getIntExtra("reminder", 0)));

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        Date date = new Date(System.currentTimeMillis());
        richEditText.setText(currentContent);
        textView.setText(currentDate);
//        textView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
//        richEditText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        if (TextUtils.isEmpty(textView.getText())) {
            textView.setText(simpleDateFormat.format(date));
        }

        currentImagePath = intent.getStringExtra("imagePath");
        if (currentImagePath != null && !currentImagePath.equals("")) {
            richEditText.insertBitmap(currentImagePath);
            Log.i("Note get", currentImagePath);
        }
        if (currentContent == null || currentContent.equals("")) {
            setTitle("New note");
        } else {
            User targetUser = dataDAO.getUserById(currentId);
            String targetName = null;
            if (targetUser != null) {
                targetName = targetUser.getNickName();
                setTitle(targetName + "'s note");
            } else {
                targetName = "unknown";
                setTitle("Public note");
            }
        }
```

**编辑**

编辑主要依靠RichEditText控件实现，而对于编辑功能，则需要判断当前用户是否已登录，如果未登录那么就只能编辑未登录用户创建的备忘。最后利用dataDAO将数据更新到数据库。

```java
//更新备忘具体实现
if (AppGlobal.USERNAME != null && !AppGlobal.USERNAME.equals("")) {
    User currentUser = UserDAO.findUser(AppGlobal.USERNAME);
    newContent = richEditText.getText().toString();
    Data newData = new Data(currentDate, newContent, date, currentPending, currentReminder, currentStar, currentUser);
    dataDAO.updateUserData(newData, currentId);
    if (lazyImgPath != null) {
        dataDAO.updateImagePath(lazyImgPath, currentId);
    }
} else {
    //目前未登录，如果targetId为-1表示原本就是未登录的时候创建的，允许修改
    //否则不允许修改
    if (targetId == -1) {
        newContent = richEditText.getText().toString();
        Data newData = new Data(currentDate, newContent, date, currentPending, currentReminder, currentStar);
        dataDAO.updateData(newData, currentId);
        if (lazyImgPath != null) {
            dataDAO.updateImagePath(lazyImgPath, currentId);
        }
    } else {
        Toast.makeText(getContext(), "Do not do bad things !", Toast.LENGTH_SHORT).show();
   

```

未登录用户编辑特定用户的笔记，失败提示

![12](C:\Users\16588\Desktop\picture\12.png)

**调用相机、读取相册**

相册、相机选择按钮

![13](C:\Users\16588\Desktop\picture\13.png)

读取相册

![14](C:\Users\16588\Desktop\picture\14.png)

调用相机

![15](C:\Users\16588\Desktop\picture\15.png)

升级了richEditText.setOnTouchListener，添加图片的关键就是使用了富文本编辑框richEditText。把图片从相册读取到备忘中，需要先获取图片本地存储的地址，然后进行转换，之后再利用BitmapFactory.decodeStream转为bitmap追加到EditText中。

```java
//插入图片关键代码
Bitmap bitmap = null;
Bitmap originalBitmap = null;
try {
    assert originalUri != null;
    originalBitmap = BitmapFactory.decodeStream(resolver.openInputStream(originalUri));
    int bitmapHeight = originalBitmap.getHeight();
    int bitmapWidth = originalBitmap.getWidth();
    double proportion = (double) bitmapHeight / (double) bitmapWidth;
    int resetWidth = 1500;
    int resetHeight = (int) (resetWidth * proportion);
    bitmap = resizeImage(originalBitmap, resetWidth, resetHeight);
} catch (FileNotFoundException e) {
    e.printStackTrace();
}
if (originalBitmap != null) {
    ImageSpan imageSpan = new ImageSpan(MemorandumActivity.this, originalBitmap);
    spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    int index = richEditText.getSelectionStart();
    Editable edit_text = richEditText.getEditableText();
    if (index < 0 || index >= edit_text.length()) {
        edit_text.append(spannableString);
    } else {
        edit_text.insert(index, spannableString);
    }
    if (TextUtils.isEmpty(richEditText.getText().toString().trim())) {
        edit_text.append("Say something for the nice pic ?\n");
    }
} else {
    Toast.makeText(MemorandumActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
}
```

而在获取图片的时候，需要以Intent.ACTION_GET_CONTENT开启一个新Intent，但这在安卓4.41之后变得不可行，需要改为ACTION_PICK。但是这样改动之后图片路径会变成google的路径（如果安装了google商店），所以在这里还需要对路径进行转换。最后的方案是继续使用ACTION_GET_CONTENT，但是需要修改"content".equalsIgnoreCase(uri.getScheme())项的转换方式。最后结合网上的方式写了一整套转换Uri的方法，根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换。

补充：在进入相册之前，需要手动动态地申请权限。

```java
//进入相册获取图片关键代码
if (ContextCompat.checkSelfPermission(MemorandumActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(MemorandumActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
} else {
    Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
    getImage.addCategory(Intent.CATEGORY_OPENABLE);
    getImage.setType("image/*");
    startActivityForResult(getImage, PHOTO_SUCCESS);
}
```

```java
/*
*图片路径转换非常关键代码
*相关代码文件：util.UriToPathUtil.java
*/
/**
 * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
 *
 * @param context
 * @param imageUri
 * @author boogie
 * @date 2021/06/06.
 */
@TargetApi(19)
public static String getImageAbsolutePath(Context context, Uri imageUri) {
    if (context == null || imageUri == null)
        return null;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
        if (isExternalStorageDocument(imageUri)) {
            String docId = DocumentsContract.getDocumentId(imageUri);
            String[] split = docId.split(":");
            String type = split[0];
            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            }
        } else if (isDownloadsDocument(imageUri)) {
            String id = DocumentsContract.getDocumentId(imageUri);
            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            return getDataColumn(context, contentUri, null, null);
        } else if (isMediaDocument(imageUri)) {
            String docId = DocumentsContract.getDocumentId(imageUri);
            String[] split = docId.split(":");
            String type = split[0];
            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            String selection = MediaStore.Images.Media._ID + "=?";
            String[] selectionArgs = new String[] { split[1] };
            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
    } // MediaStore (and general)
    else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
        // Return the remote address
        if (isGooglePhotosUri(imageUri))
            return imageUri.getLastPathSegment();
        return getDataColumn(context, imageUri, null, null);
    }
    // File
    else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
        return imageUri.getPath();
    }
    return null;
}
```

# 登录页面

界面展示：

![16](C:\Users\16588\Desktop\picture\16.png)

登录时需要输入已创建的账号，以及对应的正确的密码，之后在数据库中进行数据的查询和匹配。如果成功则会返回主页，同时已登录用户只能查看自己创建的备忘。

未登录时能查看的备忘

![17](C:\Users\16588\Desktop\picture\17.png)

登录后能查看的备忘

![18](C:\Users\16588\Desktop\picture\18.png)

```java
//登陆逻辑关键代码
f (cancel) {//非法信息
    focusView.requestFocus();//标签用于指定屏幕内的焦点View。
} else {//合法信息
    //登陆跳转逻辑
    UserDAO userDAO = new UserDAO();
    boolean sussess = userDAO.checkLogin(userName, password);
    if (sussess) {  //信息合法
        AppGlobal.NAME = userDAO.findNameByUsername(userName);//保存用户登录信息到全局变量中
        AppGlobal.USERNAME = userName;
        SharedPreferences.Editor editor = mPreferences.edit();
        if (mRememberPass.isChecked()) {
            editor.putBoolean("remember_password", true);
            editor.putString("userName", userName);
            editor.putString("password", password);
        } else {
            editor.clear();

        }
        editor.apply();
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(intent);
    } else {
        Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
    }
}
```

除此之外，用户在登陆的时候还可以选择“Remember me”，如果之后登录成功，系统则会保存登录信息，这样下次登录的时候就可以自动填入信息了。

![19](C:\Users\16588\Desktop\picture\19.png)

这实际上是使用了安卓开发常用的偏好设置来完成这项工作的。

```java
mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

mEmailView = (EditText) findViewById(R.id.email);
mPasswordView = (EditText) findViewById(R.id.password);
mRememberPass = (CheckBox) findViewById(R.id.remember_pass);

mRememberFlag = mPreferences.getBoolean("remember_password", false);

if (mRememberFlag) {
    String userName = mPreferences.getString("userName", "");
    String password = mPreferences.getString("password", "");
    mEmailView.setText(userName);
    mPasswordView.setText(password);
    mRememberPass.setChecked(true);
}
```

# 注册页面

界面展示：注册界面

![20](C:\Users\16588\Desktop\picture\20.png)

注册时需要设置昵称、设置手机号（唯一标识）、获取验证码、设置密码和正确地重复密码。遗憾的是，本App目前还没有完成验证码发送的工作，所以在这里直接点击验证码就会自动生成和填写了。

在所有信息无误之后，就会创建一名新用户，并把数据插入到数据库的User表。

注册示例

![21](C:\Users\16588\Desktop\picture\21.png)

数据库查看新增

![22](C:\Users\16588\Desktop\picture\22.png)

导航栏改变

![23](C:\Users\16588\Desktop\picture\23.png)

# 主题设置功能

界面展示：主题设置界面

![24](C:\Users\16588\Desktop\picture\24.png)

黑暗主题

![25](C:\Users\16588\Desktop\picture\25.png)

粉色主题

![26](C:\Users\16588\Desktop\picture\26.png)

在选择对应的主题之后，回发送一个无序广播，收到广播的界面都会执行recreate() 重绘界面。同时切换全局主题标识。

```java
//主题设置关键代码
@Override
public void onClick(View v) {
    switch (v.getId()) {
        case R.id.rl_blue: {
            Toast.makeText(ThemeActivity.this, "Normal...", Toast.LENGTH_SHORT).show();
            btn_normal.openSwitch();
            btn_pink.closeSwitch();
            btn_dark.closeSwitch();
            AppGlobal.THEME = "normal";
            Intent exit=new Intent();
            exit.setAction("change style");
            sendBroadcast(exit);
            break;
        }

        case R.id.rl_pink: {
            Toast.makeText(ThemeActivity.this, "Pink now !!", Toast.LENGTH_SHORT).show();
            btn_normal.closeSwitch();
            btn_pink.openSwitch();
            btn_dark.closeSwitch();
            AppGlobal.THEME = "pink";
            Intent exit=new Intent();
            exit.setAction("change style");
            sendBroadcast(exit);
            break;
        }

        case R.id.rl_night: {
            Toast.makeText(ThemeActivity.this, "Dark is cool.", Toast.LENGTH_SHORT).show();
            btn_normal.closeSwitch();
            btn_pink.closeSwitch();
            btn_dark.openSwitch();
            AppGlobal.THEME = "dark";
            Intent exit=new Intent();
            exit.setAction("change style");
            sendBroadcast(exit);
            break;
        }

        default:
            break;
    }
}
```

```java
//主题广播接收关键代码
BroadcastReceiver mybroad = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.i(TAG, "Style");
        //finish();
        recreate();
    }
};
if ("normal".equals(AppGlobal.THEME)) {
    setTheme(R.style.AppTheme);
} else if ("pink".equals(AppGlobal.THEME)) {
    setTheme(R.style.PinkTheme);
} else if ("dark".equals(AppGlobal.THEME)) {
    setTheme(R.style.DarkTheme);
}
IntentFilter filter = new IntentFilter();
filter.addAction("change style");
this.registerReceiver(mybroad, filter);
```

# 非界面组件

**GooeyMenu**
使用开源的KYGooeyMenu实现。来源：https://github.com/Crayfish2/KYGooeyMenu
这个作者实现了简单美观的粘性动画菜单可以控制menu数量，使用的时候需要实现对每个tab的监听，并且可以控制大menu和小menu之间的距离。
注意需要实现监听GooeyMenu.GooeyMenuInterface，重写menuOpen()，menuClose()，menuItemClicked(int menuNumber)方法
UI展示：

![27](C:\Users\16588\Desktop\picture\27.png)


**Toolbar**
每个页面的顶部都是一个toolbar，用来放置“返回”按钮、“保存”按钮、“导航栏”按钮等按钮。

主页toolbar

![28](C:\Users\16588\Desktop\picture\28.png)

编辑界面toolbar

![29](C:\Users\16588\Desktop\picture\29.png)

登录界面toolbar

![30](C:\Users\16588\Desktop\picture\30.png)

在使用的时候先选择要显示的控件，再者是需要重写onOptionsItemSelected方法，实现点击对应控件的行为。
```java
//toolbar关键代码
private void setActionBar() {
    actionbar = getSupportActionBar();
    //显示返回箭头默认是不显示的
    actionbar.setDisplayHomeAsUpEnabled(true);
    //显示左侧的返回箭头，并且返回箭头和title一直设置返回箭头才能显示
    actionbar.setDisplayShowHomeEnabled(true);
    actionbar.setDisplayUseLogoEnabled(true);
    //显示标题
    actionbar.setDisplayShowTitleEnabled(true);
    actionbar.setTitle("   ");
}
```

```java
//toolbar关键代码
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
        case android.R.id.home:
            mDrawerLayout.openDrawer(GravityCompat.START);
            break;
        default:
    }
    return true;
}
```

**闹钟提醒**

文件: service.AlarmService.java
基于Service类、Timer类以及NotificationManager，本App实现了一个可以定时提醒并弹窗的AlarmService类。

弹窗提醒

![31](C:\Users\16588\Desktop\picture\31.png)

![32](C:\Users\16588\Desktop\picture\32.png)

**添加通知**
```java
// 添加通知核心代码
public static void addNotification(int delayTime, String tickerText,
                                   String contentTitle, String contentText, String currentDate, int currentId, String currentImagePath) {
    Intent intent = new Intent(MemorandumActivity.getContext(),
            AlarmService.class);
    intent.putExtra("delayTime", delayTime);
    ...
    intent.putExtra("imagePath", currentImagePath);
    intent.putExtra("reminder", 1);
    MemorandumActivity.getContext().startService(intent);
}
```

此service在实现时最关键的还有重写onStartCommand方法，在这里使用了一个timer定时器并开启一个新任务，在创建时将需要弹出的通知信息以及其他需要执行的任务加入到任务的“run”中。

```java
//非常核心代码
  public int onStartCommand(final Intent intent, int flags, int startId) {

        long period = 24 * 60 * 60 * 1000;
        // 24小时一个周期
        int delay = intent.getIntExtra("delayTime", 0);
        if (null == timer) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                AlarmService.this.
                Intent notificationIntent = new Intent();
                notificationIntent.setClass(AlarmService.this, MemorandumActivity.class);
                notificationIntent.putExtra("id", intent.getIntExtra("id", 0));
                notificationIntent.putExtra("date", intent.getStringExtra("date"));
                notificationIntent.putExtra("content", intent.getStringExtra("content"));
                notificationIntent.putExtra("imagePath", intent.getStringExtra("imagePath"));
                notificationIntent.putExtra("reminder", 1);
                //增加一位以调整reminder状态
//                AlarmService.this.startActivity(notificationIntent);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Log.i("IMG", notificationIntent.getStringExtra("imagePath"));
                PendingIntent contentIntent = PendingIntent.getActivity(
                        AlarmService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                String channelId = createNotificationChannel("my_channel_ID", "my_channel_NAME", NotificationManager.IMPORTANCE_HIGH);
                Notification notification = new NotificationCompat.Builder(AlarmService.this)
                        .setContentIntent(contentIntent)
                        .setContentTitle(intent.getStringExtra("contentTitle"))
                        .setContentText(intent.getStringExtra("content"))
                        .setTicker(intent.getStringExtra("tickerText"))
                        .setSmallIcon(R.mipmap.food)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.food))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVibrate(new long[]{0, 2000, 1000, 4000})
                        .build();
                notification.flags = Notification.FLAG_INSISTENT;
                assert notificationManager != null;
                notificationManager.notify((int) System.currentTimeMillis(), notification);
            }
        }, delay, period);

        return super.onStartCommand(intent, flags, startId);
    }
```

在这里，还指定了弹窗到来时的震动，确保用户能收到弹窗通知。

补充：在使用的时候，本App先弹出一个datePickerDialog让用户选择提醒的日期，再弹出一个timePickerDialog让用户选择提醒的具体时间。

```java
//时间选择Dialog相关代码
timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
//显示时间和日期dialog
datePickerDialog.setYearRange(1985, 2028);
datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
```

之后，再重写onTimeSet方法做初步的合法性判断，之后再在其中调用AlarmService创建一个定时提醒。

```java
//重写onTimeSet方法
@Override
public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
    timeSetTotal.delete(0, timeSetTotal.length());
    timeSet = (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ? "0" + minute : minute);
    timeSetTotal = timeSetTotal.append(dateSet).append(" ").append(timeSet);
    Data data = getIntent().getParcelableExtra("data");
    currentId = intent.getIntExtra("id", 0);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    Date date;
    long notifyTime = 0;
    long currentTime = System.currentTimeMillis();
    try {
        date = simpleDateFormat.parse(timeSetTotal.toString());
        notifyTime = date.getTime();
    } catch (ParseException e) {
        e.printStackTrace();
        Log.i(TAG, "日期转换出错");
    } catch (Exception e) {
        e.printStackTrace();
        Log.i(TAG, "其他错误...");
    }
    if (notifyTime <= currentTime) {
        showToast("选择时间不能小于当前时间");
        data.setReminder(1);
        data.update(currentId);
        AlarmService.cleanAllNotification();
        return;
    }
    Log.i(TAG, timeSetTotal.toString());
    int delayTime = (int) (notifyTime - currentTime);
    currentImagePath = intent.getStringExtra("imagePath");

    AlarmService.addNotification(delayTime, "tick", "您有一条新提醒", data.getContent(), data.getDate(), data.getId(), currentImagePath);
    data.setReminder(2);
    data.update(currentId);

    showToast("已提醒");
}
```

选择日期

![33](C:\Users\16588\Desktop\picture\33.png)

选择具体时间

![34](C:\Users\16588\Desktop\picture\34.png)

# 查看个人信息
点击导航栏的头像可以查看个人信息，可以更换头像和昵称，以及查看笔记、收藏的笔记、带图片的笔记的数量
![36](C:\Users\16588\Desktop\picture\36.png)
![37](C:\Users\16588\Desktop\picture\37.png)
![38](C:\Users\16588\Desktop\picture\38.png)
```java
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_avatar:
                showCamera();
                break;
            case R.id.re_name:

                final EditText et = new EditText(this);
                et.setText(AppGlobal.NAME);
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Name change?")
//                      .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(et)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = et.getText().toString();
                                if (input.equals("")) {
                                    Toast.makeText(getApplicationContext(), "Empty name..." + input, Toast.LENGTH_LONG).show();
                                } else {
                                    userDAO.resetNickname(AppGlobal.USERNAME, input);
                                    AppGlobal.NAME = input;
                                    tv_name.setText(AppGlobal.NAME);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null).create();
//                      alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setLeft(0);
                alertDialog.show();

                break;
//            case R.id.re_fxid:
//                if (TextUtils.isEmpty(userJson.getString(HTConstant.JSON_KEY_FXID))) {
//                    startActivity(new Intent(getActivity(), ProfileUpdateActivity.class)
//                            .putExtra("type", ProfileUpdateActivity.TYPE_FXID));
//                }
//                break;
            case R.id.re_memo:
                break;
            case R.id.re_pending:
                break;
            case R.id.re_star:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                Intent it = new Intent();
                it.setClass(ProfileActivity.this, MainActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                finish();
        }
        return true;
    }

    // 拍照
    private void showCamera() {
        //弹窗
        HTAlertDialog HTAlertDialog = new HTAlertDialog(getApplicationContext(), null, new String[]{getString(R.string.attach_take_pic), getString(R.string.image_manager)});
        HTAlertDialog.init(new HTAlertDialog.OnItemClickListner() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(int position) {
                switch (position) {
                    case 0:
                        File outputImage = new File(getExternalCacheDir(), UUID.randomUUID().toString() + ".jpg");
                        AppGlobal.currentImagePath = outputImage.getAbsolutePath();
//                        Log.d("RegisterActivity", outputImagePath);
//                        Toast.makeText(RegisterActivity.this, outputImagePath, Toast.LENGTH_SHORT).show();

                        try {
                            if (outputImage.exists()) {
                                outputImage.delete();
                            }
                            outputImage.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (Build.VERSION.SDK_INT >= 24) {
                            //在拿到uri之后进行版本判断大于等于24（即Android7.0）用最新的获取路径方式，否则用之前的方式
                            head_imageUri = FileProvider.getUriForFile(ProfileActivity.this, "com.exmaple.memorandum.fileprovider", outputImage);
                        } else {
                            head_imageUri = Uri.fromFile(outputImage);
                        }
                        ActivityCompat.requestPermissions(ProfileActivity.this,
                                new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.CAMERA
                                }, 200);
                        openCamera();
                        break;
//
                    case 1:
                        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        } else {
                            openAlbum();
                        }
                        break;
//
                }
            }
        });
    }

    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, head_imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void openAlbum() {
        // Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
        // 调用相册
        // 4.41之后不可行，改为ACTION_PICK
        // 0608: 改为ACTION_PICK之后路径会变成google的路径（如果安装了google商店）
        // 继续使用ACTION_GET_CONTENT，但是需要修改"content".equalsIgnoreCase(uri.getScheme())项的转换方式
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                    openCamera();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap originalPhotoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(head_imageUri));

//                        int bitmapHeight = originalPhotoBitmap.getHeight();
//                        int bitmapWidth = originalPhotoBitmap.getWidth();
//                        double proportion = (double) bitmapHeight / (double) bitmapWidth;
//                        int resetWidth = 2000;
//                        int resetHeight = (int) (resetWidth * proportion);

                        Bitmap bitmap = CommonUtility.resizeImage(originalPhotoBitmap, 200, 200);

                        iv_avatar.setImageBitmap(bitmap);
                        // AppGlobal.INSERT_IMAGE = true;
                        userDAO.updateImagePath(AppGlobal.currentImagePath, AppGlobal.USERNAME);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        Log.d("V", "high");
                        handleImageOnKitKat(data);
                    } else {
                        Log.d("V", "low");
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    /**
     * Google相册图片获取路径
     **/
    private static Uri getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;

        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                //return writeToTempImageAndGetPathUri(context, bmp).toString();
                return writeToTempImageAndGetPathUri(context, bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
        // 有bug 会额外生成一张略缩图到相册
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.DATA, String.valueOf(inImage));
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//        Uri ans = inContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        Log.d("R0 Insert", String.valueOf(ans)) ;
//        return ans ;
//        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        intent.setData(ans);
//        inContext.sendBroadcast(intent);
//        return ans ;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(@NonNull Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("URI ANS", String.valueOf(uri));
        Log.d("URI ANS", String.valueOf(DocumentsContract.isDocumentUri(this, uri)));
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://download/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // imagePath = getImagePath(uri, null);
                if (isExternalStorageDocument(uri)) {
//                Log.i(TAG,"isExternalStorageDocument***"+uri.toString());
//                Log.i(TAG,"docId***"+docId);
//                Test print：
//                isExternalStorageDocument***content://com.android.externalstorage.documents/document/primary%3ATset%2FROC2018421103253.wav
//                docId***primary:Test/ROC2018421103253.wav
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        imagePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            } else if (isDownloadsDocument(uri)) {
//                Log.i(TAG,"isDownloadsDocument***"+uri.toString());
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                imagePath = getDataColumn(this, contentUri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                imagePath = uri.getPath();
            }
            Log.d("IMAGE ANS", imagePath);
            displayImage(imagePath);
        } else {
            if ("com.google.android.apps.photos.contentprovider".equals(uri.getAuthority())) {
                //imagePath = uri.getLastPathSegment();
                uri = getImageUrlWithAuthority(this, uri);
                imagePath = getImagePath(uri, null);
                Log.d("IMAGE ANS", imagePath);
                displayImage(imagePath);
            } else {
                Log.d("IMAGE ANS", "WTF");
            }
        }
    }

//    public boolean isExternalStorageDocument(Uri uri) {
//        return "com.android.externalstorage.documents".equals(uri.getAuthority());
//    }

//    public boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
//    }

//    public boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri.getAuthority());
//    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            File albumImage = new File(getExternalCacheDir(), UUID.randomUUID().toString() + ".jpg");
            String albumImagePath = albumImage.getAbsolutePath();
            AppGlobal.currentImagePath = albumImagePath;
            Log.d("RegisterActivity", albumImagePath);
            Toast.makeText(this, albumImagePath, Toast.LENGTH_SHORT).show();
            try {
                if (albumImage.exists()) {
                    albumImage.delete();
                }
                FileOutputStream out;
                out = new FileOutputStream(albumImage);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            iv_avatar.setImageBitmap(bitmap);
            userDAO.updateImagePath(AppGlobal.currentImagePath, AppGlobal.USERNAME);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                /**
                 * 这种方法调用图库并获取图片在android4.4版本之前是可行的，但是之后就不可行了
                 * 根本原因是前者和后者返回的URI已经不是同一个了，前者URI中包含了文件的绝对路径，是有_data的，但是后者URI就没有了。所以拿不到
                 * <4.4 URI:content://media/external/images/media/164 含有文件的绝对路径
                 *
                 * >4.4URI ：content://com.android.providers.media.documents/document/image:3951，只有文件的相对编号
                 *
                 * 两者返回的内容也有所不同
                 * 原文链接：https://blog.csdn.net/qq_32534441/article/details/103526406
                 */
                //path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        Log.d("IMAGE", path);
        return path;
    }

```



# 修改密码
如果忘记了密码，可以通过手机号获取验证码来修改密码
![35](C:\Users\16588\Desktop\picture\35.png)


```java
    private void attemptReset() {
        et_usertel.setError(null);
        et_code.setError(null);
        et_password.setError(null);
        et_password_confirm.setError(null);

        String userName = et_usertel.getText().toString().trim();
        String code = et_code.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        String password_confirm = et_password_confirm.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(userName)) {
            et_usertel.setError("Phone is empty.");
            focusView = et_usertel;
            cancel = true;
        }

        if (!userName.equals(AppGlobal.USERNAME)) {
            et_usertel.setError("Phone error.");
            focusView = et_usertel;
            cancel = true;
        }

        if (TextUtils.isEmpty(code)) {
            et_code.setError("Where is the verification code ?");
            focusView = et_code;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            et_password.setError("Password is empty.");
            focusView = et_password;
            cancel = true;
        }
        if (TextUtils.isEmpty(password_confirm)) {
            et_password_confirm.setError("Do not forget to confirm.");
            focusView = et_password_confirm;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            et_password.setError("The password requires at least four digits.");
            focusView = et_password;
            cancel = true;
        }
        if (!password.equals(password_confirm)) {
            et_password_confirm.setError("The two passwords entered are not consistent.");
            focusView = et_password_confirm;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            UserDAO userDAO = new UserDAO();
            boolean isSuccess = false;
            if (!userDAO.checkUsername(userName)) {
                et_usertel.setError("User not found.");
            } else {
                isSuccess = userDAO.checkPassword(userName, password);
                if (isSuccess) {
                    userDAO.resetPassword(userName, password);
                    Toast.makeText(ResetPasswordActivity.this, "Successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Isn't this the same as the original one?", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
```

