package ma.glasnost.orika.test.perf;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingHint;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Author;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Book;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.Library;
import ma.glasnost.orika.test.unenhance.SuperTypeTestCaseClasses.LibraryMyDTO;

import org.codehaus.janino.DebuggingInformation;
import org.codehaus.janino.JavaSourceClassLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * This test attempts to confirm that Orika doesn't cause
 * class-loaders to leak by retaining hard references to
 * classes that were not loaded by it's own class-loader or any parent
 * class-loader(s).<br><br>
 * This can cause problems specifically in web and enterprise
 * application contexts where multiple web application (siblings)
 * might share a common parent enterprise application (or shared library)
 * class-loader.<br><br>
 * 
 * @author mattdeboer
 *
 */
public class ClassLoaderLeakageTestCase {

	
	/**
	 * This initial test is to verify our own sanity
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testControl() throws Throwable {
		
		File testClassPathRoot = new File(getClass().getResource("/").getFile());
		File projectRoot = testClassPathRoot.getParentFile().getParentFile();
		
		ClassLoader threadContextLoader = Thread.currentThread().getContextClassLoader();
		
		ClassLoader childLoader = new JavaSourceClassLoader(     
				threadContextLoader,    
				new File[] { new File(projectRoot,"src/test/java-hidden") },      
				"UTF-8",                     
				DebuggingInformation.ALL         
				); 
		
		@SuppressWarnings("unchecked")
		Class<? extends Author> hiddenAuthorType = (Class<? extends Author>)childLoader.loadClass("types.AuthorHidden");
		@SuppressWarnings("unchecked")
		Class<? extends Book> hiddenBookType = (Class<? extends Book>)childLoader.loadClass("types.BookHidden");
		@SuppressWarnings("unchecked")
		Class<? extends Library> hiddenLibraryType = (Class<? extends Library>)childLoader.loadClass("types.LibraryHidden");
		
		try {
			threadContextLoader.loadClass("types.LibraryHidden");
			Assert.fail("types.LibraryHidden should not be accessible to the thread context class loader");
		} catch (ClassNotFoundException e0) {
			try {
				threadContextLoader.loadClass("types.AuthorHidden");
				Assert.fail("types.AuthorHidden should not be accessible to the thread context class loader");
			} catch (ClassNotFoundException e1) {
				try {
					threadContextLoader.loadClass("types.BookHidden");
					Assert.fail("types.BookHidden should not be accessible to the thread context class loader");
				} catch (ClassNotFoundException e2) {
					/* good: all of these types should be inaccessible */
				}
			}
		}
		
		// Now, these types are hidden from the current class-loader, but they implement types
		// that are accessible to this loader
		// -----------------------------------------------------------------------------
		
		Book book = createBook(hiddenBookType);
		book.setAuthor(createAuthor(hiddenAuthorType));
		Library lib = createLibrary(hiddenLibraryType);
		lib.getBooks().add(book);
		
		SoftReference<ClassLoader> ref = new SoftReference<ClassLoader>(childLoader);
		
		book = null;
		lib = null;
		hiddenBookType = null;
		hiddenAuthorType = null;
		hiddenLibraryType = null;
		childLoader = null;
		
		Assert.assertNotNull(ref.get());
		
		forceClearSoftAndWeakReferences();
		
		Assert.assertNull(ref.get());
		
	}
	
	
	
	
	/**
	 * This test is a bit complicated: it verifies that super-type lookup occurs properly
	 * if presented with a class that is not accessible from the current class loader, but
	 * which extends some super-type (or implements an interface) which is accessible.<br>
	 * This type of scenario might occur in web-module to ejb jar interactions...
	 *  
	 * 
	 * @throws Exception
	 */
	@Test
	public void testClassLoaderLeak() throws Exception {
		
		
		SoftReference<ClassLoader> childLoaderRef = null;
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		MappingHint myHint = 
			/**
			 * This sample hint converts "myProperty" to "property", and vis-versa.
			 */
			new MappingHint() {

				public String suggestMappedField(String fromProperty, Class<?> fromPropertyType) {
					if (fromProperty.startsWith("my")) {
						return fromProperty.substring(2, 3).toLowerCase() + fromProperty.substring(3);
					} else {
						return "my" + fromProperty.substring(0, 1).toUpperCase() + fromProperty.substring(1);
					}	
				}
				
			};
		factory.registerMappingHint(myHint);
		factory.build();
		
		MapperFacade mapper = factory.getMapperFacade();
		LibraryMyDTO mappedLib;
		{
			File testClassPathRoot = new File(getClass().getResource("/").getFile());
			File projectRoot = testClassPathRoot.getParentFile().getParentFile();
			
			ClassLoader threadContextLoader = Thread.currentThread().getContextClassLoader();
			
			ClassLoader childLoader = new JavaSourceClassLoader(     
					threadContextLoader,    
					new File[] { new File(projectRoot,"src/test/java-hidden") },      
					"UTF-8",                     
					DebuggingInformation.ALL         
					); 
			
			@SuppressWarnings("unchecked")
			Class<? extends Author> hiddenAuthorType = (Class<? extends Author>)childLoader.loadClass("types.AuthorHidden");
			@SuppressWarnings("unchecked")
			Class<? extends Book> hiddenBookType = (Class<? extends Book>)childLoader.loadClass("types.BookHidden");
			@SuppressWarnings("unchecked")
			Class<? extends Library> hiddenLibraryType = (Class<? extends Library>)childLoader.loadClass("types.LibraryHidden");
			
			try {
				threadContextLoader.loadClass("types.LibraryHidden");
				Assert.fail("types.LibraryHidden should not be accessible to the thread context class loader");
			} catch (ClassNotFoundException e0) {
				try {
					threadContextLoader.loadClass("types.AuthorHidden");
					Assert.fail("types.AuthorHidden should not be accessible to the thread context class loader");
				} catch (ClassNotFoundException e1) {
					try {
						threadContextLoader.loadClass("types.BookHidden");
						Assert.fail("types.BookHidden should not be accessible to the thread context class loader");
					} catch (ClassNotFoundException e2) {
						/* good: all of these types should be inaccessible */
					}
				}
			}
			// Now, these types are hidden from the current class-loader, but they implement types
			// that are accessible to this loader
			// -----------------------------------------------------------------------------
			
			Book book = createBook(hiddenBookType);
			book.setAuthor(createAuthor(hiddenAuthorType));
			Library lib = createLibrary(hiddenLibraryType);
			lib.getBooks().add(book);
			
			mappedLib = mapper.map(lib, LibraryMyDTO.class);
			
			// Just to be sure things mapped as expected
			Assert.assertEquals(lib.getTitle(),mappedLib.getMyTitle());
			Assert.assertEquals(book.getTitle(),mappedLib.getMyBooks().get(0).getMyTitle());
			Assert.assertEquals(book.getAuthor().getName(),mappedLib.getMyBooks().get(0).getMyAuthor().getMyName());
		
			// Now, set the soft reference before our hard references go out of scope
			childLoaderRef = new SoftReference<ClassLoader>(childLoader);
		
			book = null;
			lib = null;
			hiddenBookType = null;
			hiddenAuthorType = null;
			hiddenLibraryType = null;
			childLoader = null;
			
			factory = null;
			mapper = null;
		}
		
		Assert.assertNotNull(childLoaderRef.get());
		
		// Force GC to reclaim the soft reference
		forceClearSoftAndWeakReferences();

		// Test the target group
		Assert.assertNull(childLoaderRef.get());
		
	}
	
	
	/**
	 * Since the contract for SoftReference states that all soft references
	 * will be cleared by the garbage collector before OOME is thrown, we
	 * allocate dummy bytes until we reach OOME.
	 */
	private void forceClearSoftAndWeakReferences() {
		
		SoftReference<Object> checkReference = new SoftReference<Object>(new Object());
		Assert.assertNotNull(checkReference.get());
		try {
			List<byte[]> byteBucket = new ArrayList<byte[]>();
			for (int i=0; i < Integer.MAX_VALUE; ++i) {
				int available = (int)Math.min((long)Integer.MAX_VALUE,Runtime.getRuntime().maxMemory());
		    	byteBucket.add(new byte[available]);
			}
		} catch (Throwable e) {
		    // Ignore OME; soft references should now have been cleared 
			Assert.assertNull(checkReference.get());
		}
		
	}
	
		
	private Author createAuthor(Class<? extends Author> type) throws InstantiationException, IllegalAccessException {
		Author author = (Author) type.newInstance();
		author.setName("Khalil Gebran");
		
		return author;
	}
	
	private Book createBook(Class<? extends Book> type) throws InstantiationException, IllegalAccessException {
		Book book = (Book)type.newInstance();
		book.setTitle("The Prophet");
		
		return book;
	}
	
	private Library createLibrary(Class<? extends Library> type) throws InstantiationException, IllegalAccessException {
		Library lib = (Library)type.newInstance();
		lib.setTitle("Test Library");
		
		return lib;
	}
	
	
}
