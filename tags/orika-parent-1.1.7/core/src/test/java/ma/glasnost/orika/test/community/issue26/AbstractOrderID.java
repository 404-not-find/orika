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
package ma.glasnost.orika.test.community.issue26;

public abstract class AbstractOrderID {
	private static final long serialVersionUID = 2L;

	private Long bestellungID;

	public Long getID() {
		return bestellungID;
	}

	public AbstractOrderID() { /* Required by Orika mapping */
		this(null);
	}

	public AbstractOrderID(Long bestellungID) {
		this.bestellungID = bestellungID;
	}

	public AbstractOrderID(long bestellungID) {
		this.bestellungID = bestellungID;
	}

	public Long getBestellungID() {
		return bestellungID;
	}

	public void setBestellungID(Long bestellungID) {
		this.bestellungID = bestellungID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bestellungID == null) ? 0 : bestellungID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractOrderID other = (AbstractOrderID) obj;
		if (bestellungID == null) {
			if (other.bestellungID != null)
				return false;
		} else if (!bestellungID.equals(other.bestellungID))
			return false;
		return true;
	}
	
	
}
