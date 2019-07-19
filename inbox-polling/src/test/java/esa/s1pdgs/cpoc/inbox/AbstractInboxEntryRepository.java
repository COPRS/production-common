package esa.s1pdgs.cpoc.inbox;

import java.util.Collections;
import java.util.Optional;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntryRepository;

public abstract class AbstractInboxEntryRepository implements InboxEntryRepository {

	@Override
	public <S extends InboxEntry> S save(S entity) {
		return entity;
	}

	@Override
	public <S extends InboxEntry> Iterable<S> saveAll(Iterable<S> entities) {
		return entities;
	}

	@Override
	public Optional<InboxEntry> findById(Long id) {
		return Optional.empty();
	}

	@Override
	public boolean existsById(Long id) {
		return false;
	}

	@Override
	public Iterable<InboxEntry> findAll() {
		return Collections.emptyList();
	}

	@Override
	public Iterable<InboxEntry> findAllById(Iterable<Long> ids) {
		return Collections.emptyList();
	}

	@Override
	public long count() {
		return 0;
	}

	@Override
	public void deleteById(Long id) {	 }

	@Override
	public void delete(InboxEntry entity) {}

	@Override
	public void deleteAll(Iterable<? extends InboxEntry> entities) {}

	@Override
	public void deleteAll() {}

	@Override
	public void deleteByUrl(String url) {}
}
