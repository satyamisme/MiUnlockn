package dev.rohitverma882.miunlock

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.terminal.view.TerminalView
import com.termux.terminal.view.TerminalViewClient
import dev.rohitverma882.miunlock.databinding.ActivityTerminalBinding

class TerminalActivity : AppCompatActivity(), TerminalViewClient, TerminalSessionClient {
    private lateinit var binding: ActivityTerminalBinding

    private val terminalView: TerminalView get() = binding.terminalView
    private val currentSession: TerminalSession get() = binding.terminalView.currentSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        setupTerminalView()
    }

    private fun setupTerminalView() {
        terminalView.setTerminalViewClient(this)
        val fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            14f,
            resources.displayMetrics
        )
        terminalView.setTextSize(fontSize.toInt())
        terminalView.setIsTerminalViewKeyLoggingEnabled(false)
        val typefaceNormal = Typeface.createFromAsset(assets, "fonts/terminal_normal.ttf")
        val typefaceItalic = Typeface.createFromAsset(assets, "fonts/terminal_italic.ttf")
        terminalView.setTypeface(typefaceNormal, typefaceItalic)
        terminalView.attachSession(createTerminalSession())
    }

    private fun createTerminalSession(): TerminalSession {
        val dirs = createFilesAndCacheDirs()
        val newTerminalSession = TerminalSession(
            "/system/bin/sh",
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_terminal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_transcript -> true
            R.id.action_logout -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onTextChanged(changedSession: TerminalSession) {

    }

    override fun onTitleChanged(changedSession: TerminalSession) {

    }

    override fun onSessionFinished(finishedSession: TerminalSession) {

    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {

    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {

    }

    override fun onBell(session: TerminalSession) {

    }

    override fun onColorsChanged(session: TerminalSession) {

    }

    override fun onTerminalCursorStateChange(state: Boolean) {

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
        private const val TRANSACTION_SIZE_LIMIT_IN_BYTES = 100 * 1024
    }
}