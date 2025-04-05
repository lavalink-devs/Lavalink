FROM squidfunk/mkdocs-material

COPY docs/requirements.txt /tmp/requirements.txt

RUN pip install --no-cache-dir -r /tmp/requirements.txt

WORKDIR /docs/docs

# Start development server by default
ENTRYPOINT ["mkdocs"]
CMD ["serve", "--dev-addr=0.0.0.0:8000"]
