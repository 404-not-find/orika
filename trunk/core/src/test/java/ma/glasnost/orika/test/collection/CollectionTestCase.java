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

package ma.glasnost.orika.test.collection;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class CollectionTestCase {

	@Test
	public void testStringToString() {
		D source = new D();
		source.setTags(Arrays.asList("soa", "java", "rest"));

		A destination = MappingUtil.getMapperFactory().getMapperFacade().map(source, A.class);

		Assert.assertNotNull(destination.getTags());
		Assert.assertEquals(3, destination.getTags().size());
	}

	static public class A {
		private Set<String> tags;

		public Set<String> getTags() {
			return tags;
		}

		public void setTags(Set<String> tags) {
			this.tags = tags;
		}
	}

	public static class D {
		private List<String> tags;

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}
	}
}
