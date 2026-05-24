import { describe, it, expect, beforeEach,vi } from 'vitest';
import { render, screen } from '@testing-library/react';

/**
 * Example component test
 * 
 * This demonstrates how to test React components with the mocked dependencies.
 * Use this as a template for testing your actual components.
 */

// Simple example component for demonstration
const ExampleComponent = () => {
  return (
    <div>
      <h1>Welcome to Fin-Insight</h1>
      <p>This is an example component</p>
      <button>Click me</button>
    </div>
  );
};

describe('ExampleComponent', () => {
  beforeEach(() => {
    // Clear all mocks before each test
    vi.clearAllMocks();
  });

  it('should render the component', () => {
    render(<ExampleComponent />);
    expect(screen.getByText('Welcome to Fin-Insight')).toBeInTheDocument();
  });

  it('should render a button', () => {
    render(<ExampleComponent />);
    const button = screen.getByRole('button', { name: /click me/i });
    expect(button).toBeInTheDocument();
  });

  it('should display descriptive text', () => {
    render(<ExampleComponent />);
    expect(
      screen.getByText('This is an example component')
    ).toBeInTheDocument();
  });
});
