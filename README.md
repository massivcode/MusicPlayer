
# Simple Music Player
롤리팝에서 추가된 머테리얼 디자인을 최대한 적용한 심플한 안드로이드 뮤직 플레이어 입니다.

기존에 사용하던 Google의 Play Music이 필요 없는 기능이 너무 많아 불만을 가지고 있던 중 자급자족 해보는 건 어떨까 하는 생각이 들어 한번 만들어 보게 되었습니다.

## 개발 환경
* 개발 툴 : Android Studio 1.5
* SDK 버전 : 
    * minSdkVersion 16
    * targetSdkVersion 23
    * compileSdkVersion 23
* 테스트 
    * JUnit4
    * LeakCanary : Memory Leak 테스트 도구
    * Monkey : Android SDK에서 제공하는 UI 테스트 도구
* 버전관리 : Git

## 개발이력 및 기간 (2015.11.23 ~ 2015.12.07)
![개발기간](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/history.png)

# UI
![네비게이션](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/navigation_view.png)
<br>
레이아웃은 ViewPager, TabLayout, NavigationDrawer 을 이용하여 구성하였습니다.
<br>
![플레이어](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/player.png)
<br>
위의 화면이 처음 앱을 시작했을 때 보이는 화면으로 플레이어, 재생목록, 아티스트, 노래의 4개 화면 중 첫번째 화면입니다.
<br>
![재생목록_Null](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/playlist_none.png)
<br>
다음으로 두번째 화면인 재생목록 화면입니다. 하단의 FloatingActionButton을 통해 사용자 정의 플레이리스트를 추가할 수 있습니다.
<br>
![재생목록_search_null](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/search_none.png)
<br>
FAB를 눌러 진입한 AddPlaylistActivity의 화면입니다. 기본적으론 비어있습니다.
<br>
![재생목록_songs](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/search_songs.png)
<br>
상단의 돋보기 아이콘을 눌러 SearchView를 이용해 얻은 음원들을 이용하거나,
<br>
![재생목록_searched](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/search_searched.png)
<br>
음표 모양의 아이콘을 눌러 기기의 모든 음원을 이용할 수 있습니다.
<br>
![재생목록_checked](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/add_songs_checked.png)
<br>
추가하고자 하는 음원들을 선택하면 다음과 같이 테두리의 색상이 변경되며 하단의 FAB를 클릭하면 재생목록을 저장하기 위한 Dialog가 팝업되는데,
<br>
![재생목록_add](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/add_songs_dialog.png)
<br>
여기서 아이템들을 누르면 추가하기 위한 목록에서 제거되며, 재생목록의 이름을 입력하고 저장 버튼을 누르면 저장됩니다.
<br>
![재생목록_added](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/playlist_added.png)
<br>
사용자 정의 플레이리스트가 추가된 재생목록 프래그먼트 입니다.
<br>
![재생목록_delete](https://github.com/prChoe/MusicPlayer/blob/master/doc/playlist_delete.png)
<br>
아이템을 롱클릭하면 삭제를 진행하기 위해 팝업을 띄웁니다.
<br>
![아티스트](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/artist.png)
<br>
세번째 탭인 아티스트 입니다. 기기에 존재하는 모든 음원을 가수별로 나눠서 출력합니다. Child Item을 클릭하면 재생되며, 현재 재생 중이거나 일시정지 중인 음원의 상태를 알 수 있습니다.
<br>
![노래](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/songs.png)
<br>
마지막 탭인 노래 입니다. 기기에 존재하는 모든 음원을 출력하며, Item을 클릭하면 해당 곡만 재생을 하고 ListView의 헤더 버튼을 클릭하면 모든 음원을 재생합니다. 우측에 위치한 ScrollBar을 이용하여 빠른 스크롤을 할 수 있습니다.
<br>
![현재 재생](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/current_playlist.png)
<br>
액션바에 위치한 리스트 아이콘을 클릭하면 현재 재생 중인 음원들을 표시하는 DialogFragment를 팝업합니다.
<br>
![노티_롤리팝](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/notification_lollipop.png)
<br>
롤리팝 이상의 알림바에서는 위와 같은 알림이 표시됩니다. 이전/다음 곡 재생, 재생/일시정지, 앱 종료 기능이 있습니다.
<br>
![잠금화면](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/lockscreen.png)
<br>
또한 잠금화면에서도 알림이 표시됩니다.
<br>
![노티_젤리빈](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/notification_jellybean.png)
<br>
젤리빈 버전에서는 Notification의 크기적인 문제나, 액션의 개수 제한, 그리고 확장알림을 기본적으로 활성화할 수 없어서 RemoteView를 이용하여 레이아웃을 구성하였습니다.


# Structure (XMind)
![구조](https://raw.githubusercontent.com/prChoe/MusicPlayer/master/doc/structure.PNG)

# Features
* 플레이어, 재생목록, 가수, 노래 목록 제공
* 현재 재생 중인 곡 목록 제공
* 목록에서의 빠른 스크롤
* 알림바/잠금화면에서 이전/다음 곡 넘김, 재생/일시정지, 앱 종료
    * 잠금화면에서의 조작은 Android Lollipop 부터 가능합니다.
* 1회 재생, 전체 반복, 셔플
* 사용자 정의 플레이리스트, 쉽게 추가하는 즐겨찾기
* 이전에 재생하던 곡 목록을 실행시 복원
* 이어폰이 언플러그 되었을 때 자동 정지





# Credits
1. 다이나믹 비트맵 로딩 라이브러리
   * junsuk5 / AsyncBitmapLoader : https://github.com/junsuk5/AsyncBitmapLoader

2. 안드로이드 이벤트 버스
    * greenrobot / EventBus : https://github.com/greenrobot/EventBus
    
3. Memory Leak 검출 라이브러리
    * square / leakcanary : https://github.com/square/leakcanary

4. 크롬을 이용한 DB Browser 라이브러리
    * facebook / stetho : https://github.com/facebook/stetho

5. 몽키 테스트
    * http://developer.android.com/intl/ko/tools/help/monkey.html

# License

Copyright 2015. Pureum Choe
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 
