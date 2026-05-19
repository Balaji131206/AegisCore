import React from 'react';
import Layout from '@theme/Layout';

export default function Home() {
  return (
    <Layout title="Home" description="Project documentation for MultiThreadSystemJAVA">
      <main className="container margin-vert--lg">
        <h1>Welcome to MultiThreadSystemJAVA</h1>
        <p>
          This documentation site is built with Docusaurus and uses the existing
          <code>docs/</code> folder as the content source.
        </p>
        <p>
          Click <a href="/docs/intro">Docs</a> to get started with setup,
          architecture, networking, and testing guides.
        </p>
      </main>
    </Layout>
  );
}
