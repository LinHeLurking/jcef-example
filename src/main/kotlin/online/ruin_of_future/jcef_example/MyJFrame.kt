package online.ruin_of_future.jcef_example

import org.cef.CefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefFocusHandlerAdapter
import java.awt.BorderLayout
import java.awt.Component
import java.awt.KeyboardFocusManager
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JTextField


class MyJFrame : JFrame() {
    private val serialVersionUID = -5570653778104813836L
    private var address: JTextField? = null
    private var cefApp: CefApp? = null
    private var client: CefClient? = null
    private var browser: CefBrowser? = null
    private var browserUi: Component? = null
    private var browserFocus = true

    /**
     * To display a simple browser window, it suffices completely to create an
     * instance of the class CefBrowser and to assign its UI component to your
     * application (e.g. to your content pane).
     * But to be more verbose, this CTOR keeps an instance of each object on the
     * way to the browser UI.
     */
    fun mainFrame(startURL: String, useOSR: Boolean, isTransparent: Boolean, args: Array<String>) {
        // (1) The entry point to JCEF is always the class CefApp. There is only one
        //     instance per application, and therefore you have to call the method
        //     "getInstance()" instead of a CTOR.
        //
        //     CefApp is responsible for the global CEF context. It loads all
        //     required native libraries, initializes CEF accordingly, starts a
        //     background task to handle CEF's message loop and takes care of
        //     shutting down CEF after disposing it.
        //
        //     WHEN WORKING WITH MAVEN: Use the builder.build() method to
        //     build the CefApp on first run and fetch the instance on all consecutive
        //     runs. This method is thread-safe and will always return a valid app
        //     instance.
        cefApp = CefAppBuilder().build()

        // (2) JCEF can handle one to many browser instances simultaneous. These
        //     browser instances are logically grouped together by an instance of
        //     the class CefClient. In your application you can create one to many
        //     instances of CefClient with one to many CefBrowser instances per
        //     client. To get an instance of CefClient you have to use the method
        //     "createClient()" of your CefApp instance. Calling an CTOR of
        //     CefClient is not supported.
        //
        //     CefClient is a connector to all possible events which come from the
        //     CefBrowser instances. Those events could be simple things like the
        //     change of the browser title or more complex ones like context menu
        //     events. By assigning handlers to CefClient you can control the
        //     behavior of the browser. See tests.detailed.MainFrame for an example
        //     of how to use these handlers.
        client = cefApp?.createClient()

        // (3) Create a simple message router to receive messages from CEF.
        val msgRouter = CefMessageRouter.create()
        client?.addMessageRouter(msgRouter)

        // (4) One CefBrowser instance is responsible to control what you'll see on
        //     the UI component of the instance. It can be displayed off-screen
        //     rendered or windowed rendered. To get an instance of CefBrowser you
        //     have to call the method "createBrowser()" of your CefClient
        //     instances.
        //
        //     CefBrowser has methods like "goBack()", "goForward()", "loadURL()",
        //     and many more which are used to control the behavior of the displayed
        //     content. The UI is held within a UI-Compontent which can be accessed
        //     by calling the method "getUIComponent()" on the instance of CefBrowser.
        //     The UI component is inherited from a java.awt.Component and therefore
        //     it can be embedded into any AWT UI.
        browser = client?.createBrowser(startURL, useOSR, isTransparent)
        browserUi = browser?.uiComponent

        // (5) For this minimal browser, we need only a text field to enter an URL
        //     we want to navigate to and a CefBrowser window to display the content
        //     of the URL. To respond to the input of the user, we're registering an
        //     anonymous ActionListener. This listener is performed each time the
        //     user presses the "ENTER" key within the address field.
        //     If this happens, the entered value is passed to the CefBrowser
        //     instance to be loaded as URL.
        address = JTextField(startURL, 100)
        address?.addActionListener { browser?.loadURL(address?.text) }

        // Update the address field when the browser URL changes.
        client?.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onAddressChange(browser: CefBrowser, frame: CefFrame, url: String) {
                address?.text = url
            }
        })

        // Clear focus from the browser when the address field gains focus.
        address?.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                if (!browserFocus) return
                browserFocus = false
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                address?.requestFocus()
            }
        })

        // Clear focus from the address field when the browser gains focus.
        client?.addFocusHandler(object : CefFocusHandlerAdapter() {
            override fun onGotFocus(browser: CefBrowser) {
                if (browserFocus) return
                browserFocus = true
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                browser.setFocus(true)
            }

            override fun onTakeFocus(browser: CefBrowser, next: Boolean) {
                browserFocus = false
            }
        })

        // (6) All UI components are assigned to the default content pane of this
        //     JFrame and afterwards the frame is made visible to the user.
        address?.let { contentPane.add(it, BorderLayout.NORTH) }
        browserUi?.let { contentPane.add(it, BorderLayout.CENTER) }
        pack()
        setSize(800, 600)
        isVisible = true

        // (7) To take care of shutting down CEF accordingly, it's important to call
        //     the method "dispose()" of the CefApp instance if the Java
        //     application will be closed. Otherwise, you'll get asserts from CEF.
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                CefApp.getInstance().dispose()
                dispose()
            }
        })
    }
}