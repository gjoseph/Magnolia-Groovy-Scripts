import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import static info.magnolia.nodebuilder.Ops.*
import info.magnolia.cms.core.Content

/**
 * Grabs and filters content of Wikipedia, and creates Magnolia pages off of it.
 * Useful for quick demos and tests.
 * I love jsoup.
 */
class GrabWikipedia {
    
    static flush(Content root) {
        root.getChildren().each {
            if (!it.sourceURL) {
                println "Skipping ${it.getName()}"
                return
            }
            println "Deleting ${it.getName()}"
            it.delete()
        }
        root.save()
    }

    static demo(root) {
        def g = new GrabWikipedia()
        g.grabAndCopy(root, 'James_Franco')
        g.grabAndCopy(root, 'Danny_Boyle')
        g.grabAndCopy(root, 'Seth_Rogen')
        g.grabAndCopy(root, 'James_Dean')
        // for some reason this page times out quite often :D g.grabAndCopy(root, 'Barack_Obama')
        g.grabAndCopy(root, 'George_W._Bush')
        g.grabAndCopy(root, 'Steve_Jobs')
        g.grabAndCopy(root, 'Bill_Gates')
        g.grabAndCopy(root, 'Johann_Sebastian_Bach')
        g.grabAndCopy(root, 'Stephen_Malkmus')
        g.grabAndCopy(root, 'Black_Francis')
        g.grabAndCopy(root, 'Rivers_Cuomo')

        g.grabAndCopy(root, 'Los_Angeles')
        g.grabAndCopy(root, 'Boston')
        g.grabAndCopy(root, 'Namur_(city)')
        g.grabAndCopy(root, 'Brussels')
        g.grabAndCopy(root, 'Basel')
        g.grabAndCopy(root, 'Mendoza,_Argentina')
        g.grabAndCopy(root, 'Paris')
        g.grabAndCopy(root, 'Goreme') // Gšreme
        g.grabAndCopy(root, 'Istanbul')
        root.save()
    }

    def grabAndCopy(Content parent, String wikipediaTitle) {
        def wp = grabWikipedia(wikipediaTitle)
        def pageName = wikipediaTitle.toLowerCase().replace("_", "-").replaceAll("[^a-z0-9-]", "")
        createPage(parent, pageName, wp.title, wp.content, wp.url)
    }

    @Grab(group = 'org.jsoup', module = 'jsoup', version = '1.6.1')
    def grabWikipedia(path) {
        final URL url = new URL("http://en.wikipedia.org/wiki/${path}")
        println "Grabbing ${url}"
        final Document doc = Jsoup.parse(url, 1000)

        def title = doc.select("title").first().text().replace(" - Wikipedia, the free encyclopedia", "")
        final Element content = doc.select("#bodyContent .mw-content-ltr").first()
        content.select("table").remove()
        content.select(".references").remove()
        content.select(".references-small").remove()
        content.select(".editsection").remove()
        content.select("div").remove()

        return [title: title, content: content.html(), url:url]
    }

    def createPage(parent, pageName, title, htmlContent, sourceURL) {
        new info.magnolia.nodebuilder.NodeBuilder(parent,
                addNode(pageName, 'mgnl:content').then(
                        getNode('MetaData').then(
                                addProperty('mgnl:template', 'stkArticle'),
                                addProperty('mgnl:title', title)
                        ),
                        addProperty('sourceURL', sourceURL),
                        addProperty('title', title),
                        addNode('main', 'mgnl:contentNode').then(
                                addNode('1', 'mgnl:contentNode').then(
                                        getNode('MetaData').then(
                                                addProperty('mgnl:template', 'stkTextImage')
                                        ),
                                        addProperty('text', htmlContent)

                                ),
                        )
                )
        ).exec()
    }
}
