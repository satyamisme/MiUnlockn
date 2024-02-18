package dev.rohitverma882.miunlock

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.termux.terminal.BellHandler
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.terminal.view.TerminalView
import com.termux.terminal.view.TerminalViewClient

import dev.rohitverma882.miunlock.databinding.ActivityTerminalBinding

import org.json.JSONObject

class TerminalActivity : AppCompatActivity(), TerminalViewClient, TerminalSessionClient {
    private lateinit var binding: ActivityTerminalBinding

    private val terminalView: TerminalView get() = binding.terminalView
    private val currentSession: TerminalSession get() = binding.terminalView.currentSession
    private var isVisible: Boolean = true

    private var terminalCursorBlinkerStateAlreadySet: Boolean = false
    private var showSoftKeyboardIgnoreOnce: Boolean = true
    private var showSoftKeyboardRunnable: Runnable? = null

    private val shellExec = "/system/bin/sh"
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("user", User::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra("user") as User?
        }

        loginUser()
        saveUserData()

        binding = ActivityTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupTerminalView()
    }

    private fun saveUserData() {
        try {
            val json = JSONObject()
            json.put("passToken", user?.passToken)
            json.put("userId", user?.userId)
            json.put("deviceId", user?.deviceId)

            ApplicationLoader.prefs.edit()
                .putString("user", json.toString())
                .apply()
        } catch (e: Throwable) {
            ApplicationLoader.prefs.edit()
                .putString("user", "")
                .apply()

            Toast.makeText(
                this,
                getString(R.string.msg_user_data_save_error, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loginUser() {
        if (user == null) {
            startLoginScreen(false)
        }
    }

    private fun logoutUser() {
        startLoginScreen(true)
    }

    private fun startLoginScreen(isLogout: Boolean) {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("isLogout", isLogout)
        }.also {
            startActivity(it)
            finish()
        }
    }

    private fun setupTerminalView() {
        val fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            TERMINAL_FONT_SIZE,
            resources.displayMetrics
        )
        val typefaceNormal =
            Typeface.createFromAsset(assets, "fonts/terminal_normal.ttf")
        val typefaceItalic =
            Typeface.createFromAsset(assets, "fonts/terminal_italic.ttf")

        terminalView.setTerminalViewClient(this)
        terminalView.setTextSize(fontSize.toInt())
        terminalView.setIsTerminalViewKeyLoggingEnabled(false)
        terminalView.setTypeface(typefaceNormal, typefaceItalic)
        terminalView.attachSession(createTerminalSession())
    }

    private fun createTerminalSession(): TerminalSession {
        val dirs = createFilesAndCacheDirs()
        val newTerminalSession = TerminalSession(
            shellExec,
            dirs[0],
            arrayOf<String>(),
            createTerminalEnvs(),
            DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
            this
        )
        newTerminalSession.mSessionName = "Terminal"
        return newTerminalSession
    }

    private fun createTerminalEnvs(): Array<String?> {
        val envs = arrayOfNulls<String>(3)
        val dirs = createFilesAndCacheDirs()
        envs[0] = "HOME=${dirs[0]}"
        envs[1] = "TMPDIR=${dirs[1]}"
        envs[2] = "TERM=screen"
        return envs
    }

    private fun createFilesAndCacheDirs(): Array<String?> {
        val dirs = arrayOfNulls<String>(2)
        if (!filesDir.exists()) filesDir.mkdirs()
        dirs[0] = filesDir.absolutePath
        if (!cacheDir.exists()) cacheDir.mkdirs()
        dirs[1] = cacheDir.absolutePath
        return dirs
    }

    override fun onStart() {
        super.onStart()
        isVisible = true

        terminalView.onScreenUpdated()
    }

    override fun onStop() {
        super.onStop()
        isVisible = false

        setTerminalCursorBlinkerState(false)
    }

    override fun onResume() {
        super.onResume()
        setSoftKeyboardState()

        terminalCursorBlinkerStateAlreadySet = false
        if (terminalView.mEmulator != null) {
            setTerminalCursorBlinkerState(true)
            terminalCursorBlinkerStateAlreadySet = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_terminal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun copyTextToClipboard(text: CharSequence) {
        val clipboard = getSystemService(ClipboardManager::class.java)
        val clip = ClipData.newPlainText("terminal", text)
        if (clipboard == null || clip == null) return
        clipboard.setPrimaryClip(clip)
    }

    private fun setSoftKeyboardState() {
        terminalView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                if (showSoftKeyboardIgnoreOnce) {
                    showSoftKeyboardIgnoreOnce = false
                    return@setOnFocusChangeListener
                }
                view.postDelayed(getShowSoftKeyboardRunnable(), 500)
            } else {
                view.removeCallbacks(showSoftKeyboardRunnable)
                hideSoftInputKeyboard()
            }
        }
        if (!showSoftKeyboardIgnoreOnce) {
            terminalView.requestFocus()
            terminalView.postDelayed(getShowSoftKeyboardRunnable(), 300)
        }
    }

    private fun getShowSoftKeyboardRunnable(): Runnable? {
        if (showSoftKeyboardRunnable == null) {
            showSoftKeyboardRunnable = Runnable {
                showSoftInputKeyboard()
            }
        }
        return showSoftKeyboardRunnable
    }

    private fun showSoftInputKeyboard() {
        terminalView.requestFocus()
        val imm = getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(terminalView, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideSoftInputKeyboard() {
        terminalView.requestFocus()
        val imm = getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(terminalView.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun setTerminalCursorBlinkerState(start: Boolean) {
        if (start) {
            if (terminalView.setTerminalCursorBlinkerRate(0)) {
                terminalView.setTerminalCursorBlinkerState(true, true)
            }
        } else {
            terminalView.setTerminalCursorBlinkerState(false, true)
        }
    }

    override fun onTextChanged(changedSession: TerminalSession) {
        if (!isVisible) return
        if (currentSession == changedSession) {
            terminalView.onScreenUpdated()
        }
    }

    override fun onTitleChanged(changedSession: TerminalSession) {

    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        finish()
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {
        if (!isVisible || text.isNullOrEmpty()) return
        copyTextToClipboard(text)
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {

    }

    override fun onBell(session: TerminalSession) {
        if (!isVisible) return
        BellHandler.getInstance(this).doBell()
    }

    override fun onColorsChanged(session: TerminalSession) {

    }

    override fun onTerminalCursorStateChange(state: Boolean) {
        if (state && !isVisible) return
        terminalView.setTerminalCursorBlinkerState(state, false)
    }

    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {

    }

    override fun getTerminalCursorStyle(): Int {
        return TerminalEmulator.DEFAULT_TERMINAL_CURSOR_STYLE
    }

    override fun onScale(scale: Float): Float {
        return 1.0f
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        showSoftInputKeyboard()
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean {
        return false
    }

    override fun shouldEnforceCharBasedInput(): Boolean {
        return true
    }

    override fun shouldUseCtrlSpaceWorkaround(): Boolean {
        return false
    }

    override fun isTerminalViewSelected(): Boolean {
        return true
    }

    override fun copyModeChanged(copyMode: Boolean) {

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && terminalView.mEmulator == null) {
            finish()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && !session?.isRunning!!) {
            finish()
            return true
        }
        return false
    }

    override fun onLongPress(event: MotionEvent?): Boolean {
        return false
    }

    override fun readControlKey(): Boolean {
        return false
    }

    override fun readAltKey(): Boolean {
        return false
    }

    override fun readShiftKey(): Boolean {
        return false
    }

    override fun readFnKey(): Boolean {
        return false
    }

    override fun onCodePoint(
        codePoint: Int,
        ctrlDown: Boolean,
        session: TerminalSession?
    ): Boolean {
        return false
    }

    override fun onEmulatorSet() {
        if (!terminalCursorBlinkerStateAlreadySet) {
            setTerminalCursorBlinkerState(true)
            terminalCursorBlinkerStateAlreadySet = true
        }
    }

    override fun logError(tag: String?, message: String?) {

    }

    override fun logWarn(tag: String?, message: String?) {

    }

    override fun logInfo(tag: String?, message: String?) {

    }

    override fun logDebug(tag: String?, message: String?) {

    }

    override fun logVerbose(tag: String?, message: String?) {

    }

    override fun logStackTrace(tag: String?, e: Exception?) {

    }

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {

    }

    companion object {
        private val TAG = TerminalActivity::class.java.simpleName
        private const val TERMINAL_FONT_SIZE = 14f
    }
}