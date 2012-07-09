/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.test.perf;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.ConcurrentRule;
import ma.glasnost.orika.test.ConcurrentRule.Concurrent;
import ma.glasnost.orika.test.DynamicSuite;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.common.types.TestCaseClasses.AuthorImpl;
import ma.glasnost.orika.test.common.types.TestCaseClasses.Book;
import ma.glasnost.orika.test.common.types.TestCaseClasses.BookImpl;
import ma.glasnost.orika.test.common.types.TestCaseClasses.Library;
import ma.glasnost.orika.test.common.types.TestCaseClasses.LibraryDTO;
import ma.glasnost.orika.test.common.types.TestCaseClasses.LibraryImpl;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class MultiThreadedTestCase {

	
	/**
	 * Allows us to run methods concurrently by marking with <code>@Concurrent</code>;
	 * note that in the current implementation, such methods will have the
	 * <code>@Before</code> and <code>@After</code> methods also invoked concurrently.
	 */
	@Rule
	public ConcurrentRule concurrentRule = new ConcurrentRule();
	
	private volatile Set<Class<?>> classes = getNonAnonymousClasses();
	
	
	private final MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
	
	private static Set<Class<?>> getNonAnonymousClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		File classFolder;
		try {
			classFolder = new File(URLDecoder.decode(MultiThreadedTestCase.class.getResource("/")
					.getFile(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		List<Class<?>> allClasses = DynamicSuite.findTestCases(classFolder, ".*");
		for (Class<?> aClass: allClasses) {
			if (!aClass.isAnonymousClass()) {
				classes.add(aClass);
			}
		}
		return classes;
	}
	
	private final AtomicInteger threadIndex = new AtomicInteger(0);
	private Type<?>[] typeResults = new Type<?>[15];
	private CountDownLatch finishLine = new CountDownLatch(15);
	
	@Test
	@Concurrent(15)
	public void testDefineSingleTypeSimultaneously() throws InterruptedException {
		
		int myIndex = threadIndex.getAndAdd(1);
		typeResults[myIndex] = TypeFactory.valueOf(Integer.class);
		
		finishLine.countDown();

		finishLine.await();
		
		Type<?> firstType = typeResults[0];
		for (Type<?> type: typeResults) {
			Assert.assertEquals(firstType, type);
		}
	}
	
	
	AtomicInteger myIndex = new AtomicInteger();
	
	/**
	 * Verifies that multiple threads requesting the type for the same set of classes receive
	 * the same set of values over a large number of classes.
	 */
	@Test
	@Concurrent(100)
	public void testDefineTypesSimultaneously() {
		
		int i = myIndex.getAndIncrement();
		int c = 0;
		Map<Type<?>,Class<?>> types = new HashMap<Type<?>,Class<?>>();
		for (Class<?> aClass: classes) {
			
			/*
			 * In this section, we force each of the threads to trigger a GC 
			 * at some point in mid process; this should help shake out any 
			 * issues with weak references getting cleared (that we didn't expect to
			 * be cleared).
			 */
			++c;
			if (c == i) {
				forceClearSoftAndWeakReferences();
			}
			Type<?> aType;
			try {
				aType = TypeFactory.valueOf(aClass);
			} catch (StackOverflowError e) {
				throw new RuntimeException("while trying to evaluate valueOf(" + aClass.getCanonicalName() + ")", e);
			}
			if (aType == null) {
				throw new IllegalStateException("TypeFactory.valueOf() returned null for " + aClass);
			} else if (types.containsKey(aType)) {
				throw new IllegalStateException("mapping already exists for " + aClass + ": " + aType + " = " + types.get(aType));
			} else {
				if (aClass.isAssignableFrom(aType.getRawType())) {
					types.put(aType, aClass);
				} else {
					throw new IllegalStateException(aType + " is not an instance of " + aClass);
				}
			} 
		}
		
		Assert.assertEquals(classes.size(), types.size());
	}
	
	@Test
	@Concurrent(20)
	public void testGenerateMappers() {
		BookImpl book = new BookImpl("The Book Title", new AuthorImpl("The Author Name"));
		Library lib = new LibraryImpl("The Library", Arrays.<Book>asList(book));
		
		LibraryDTO mappedLib = mapper.map(lib, LibraryDTO.class);
		
		// Just to be sure things mapped as expected
		Assert.assertEquals(lib.getTitle(),mappedLib.getTitle());
		Assert.assertEquals(book.getTitle(),mappedLib.getBooks().get(0).getTitle());
		Assert.assertEquals(book.getAuthor().getName(),mappedLib.getBooks().get(0).getAuthor().getName());
	
	}
	
	
	@Test
	@Concurrent(20)
	public void testGenerateObjectFactories() {
        
        Person person = new Person();
        person.setFirstName("Abdelkrim");
        person.setLastName("EL KHETTABI");
        Calendar cal = Calendar.getInstance();
        cal = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
        cal.add(Calendar.YEAR, -31);
        person.setDateOfBirth(cal.getTime());
        person.setAge(31L);
        
        PersonVO vo = mapper.map(person, PersonVO.class);
        
        Assert.assertEquals(person.getFirstName(), vo.getFirstName());
        Assert.assertEquals(person.getLastName(), vo.getLastName());
        Assert.assertTrue(person.getAge() == vo.getAge());
        Assert.assertEquals(cal.getTime(), vo.getDateOfBirth());
	}
	
	public static class Person {
        private String firstName;
        private String lastName;
        
        private Long age;
        private Date date;
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public Long getAge() {
            return age;
        }
        
        public void setAge(Long age) {
            this.age = age;
        }
        
        public Date getDateOfBirth() {
            return date;
        }
        
        public void setDateOfBirth(Date date) {
            this.date = date;
        }
        
    }
    
    public static class PersonVO {
        private final String firstName;
        private final String lastName;
        
        private final long age;
        private final Date dateOfBirth;
        
        public PersonVO(String firstName, String lastName, long age, Date dateOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.dateOfBirth = dateOfBirth;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public long getAge() {
            return age;
        }
        
        public Date getDateOfBirth() {
            return dateOfBirth;
        }
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
	
	
}
