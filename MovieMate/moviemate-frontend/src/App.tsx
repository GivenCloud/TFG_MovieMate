import { toast } from 'sonner'
import './App.css'

function App() {

  return (
    <div>
      <button onClick={() => toast.success('¡MovieMate listo!')}>Test Toast</button>
    </div>
  )
}

export default App
